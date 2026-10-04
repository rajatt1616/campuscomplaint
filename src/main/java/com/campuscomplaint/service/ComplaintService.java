package com.campuscomplaint.service;

import com.campuscomplaint.exception.UnauthorizedException;
import com.campuscomplaint.model.Category;
import com.campuscomplaint.model.Complaint;
import com.campuscomplaint.model.ComplaintStatus;
import com.campuscomplaint.model.Priority;
import com.campuscomplaint.model.Remark;
import com.campuscomplaint.model.User;
import com.campuscomplaint.store.JsonDataStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ComplaintService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DUE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").withZone(ZONE);

    private final JsonDataStore store;
    private final AuthService auth;
    private final NotificationService notifications;
    private final Duration autoCloseWindow;
    private final Duration reopenWindow;

    public ComplaintService(JsonDataStore store,
                            AuthService auth,
                            NotificationService notifications,
                            @Value("${app.deadline.auto-close}") Duration autoCloseWindow,
                            @Value("${app.deadline.reopen}") Duration reopenWindow) {
        this.store = store;
        this.auth = auth;
        this.notifications = notifications;
        this.autoCloseWindow = autoCloseWindow;
        this.reopenWindow = reopenWindow;
    }

    public Complaint create(User actor, String title, String description,
                            Category category, Priority priority) {
        if (!actor.isStudent()) {
            throw new UnauthorizedException("Only students can file complaints");
        }
        Complaint c = new Complaint();
        c.setId(store.nextComplaintId());
        c.setTitle(title);
        c.setDescription(description);
        c.setCategory(category);
        c.setPriority(priority);
        c.setStatus(ComplaintStatus.FILED);
        c.setCreatedById(actor.getId());
        c.setCreatedByName(actor.getName());
        Instant now = Instant.now();
        c.setCreatedAt(now);
        c.setUpdatedAt(now);
        c.setDeadlineAt(now.plus(autoCloseWindow));
        store.getComplaints().add(c);
        store.saveComplaints();
        return c;
    }

    public Complaint assign(User actor, String complaintId, String departmentUserId) {
        requireAdmin(actor);
        Complaint c = require(complaintId);
        if (c.getStatus() != ComplaintStatus.FILED) {
            throw new UnauthorizedException("Only newly filed complaints can be assigned");
        }
        User dept = auth.requireById(departmentUserId);
        if (!dept.isDepartment()) {
            throw new UnauthorizedException("Target user is not a department");
        }
        c.setAssignedToId(dept.getId());
        c.setAssignedToName(dept.getName());
        c.moveTo(ComplaintStatus.ASSIGNED, actor.getName(), "Assigned to " + dept.getName());
        store.saveComplaints();
        String dueMsg = c.getDeadlineAt() != null ? " — due " + DUE_FORMAT.format(c.getDeadlineAt()) : "";
        notifyUser(dept.getId(), c.getId(), c.getId() + " assigned to you" + dueMsg);
        notifyUser(c.getCreatedById(), c.getId(),
                c.getId() + " assigned to " + dept.getName());
        return c;
    }

    public Complaint startWork(User actor, String complaintId) {
        Complaint c = requireAssignedTo(actor, complaintId);
        c.moveTo(ComplaintStatus.IN_PROGRESS, actor.getName(), "Work started");
        store.saveComplaints();
        notifyUser(c.getCreatedById(), c.getId(),
                actor.getName() + " started working on " + c.getId());
        return c;
    }

    public Complaint markResolved(User actor, String complaintId, String remarkText) {
        Complaint c = requireAssignedTo(actor, complaintId);
        c.moveTo(ComplaintStatus.RESOLVED, actor.getName(), "Marked as resolved");
        if (remarkText != null && !remarkText.isBlank()) {
            c.addRemark(new Remark(actor.getId(), actor.getName(), actor.getRole().name(),
                    remarkText.trim(), Instant.now()));
        }
        store.saveComplaints();
        notifyUser(c.getCreatedById(), c.getId(),
                c.getId() + " marked resolved — verify and accept");
        return c;
    }

    public Complaint accept(User actor, String complaintId) {
        Complaint c = requireOwnedBy(actor, complaintId);
        c.moveTo(ComplaintStatus.CLOSED, actor.getName(), "Accepted by complainant");
        Instant now = Instant.now();
        c.setClosedAt(now);
        c.setReopenDeadlineAt(now.plus(reopenWindow));
        c.setDeadlineAt(null);
        store.saveComplaints();
        notifyUser(c.getAssignedToId(), c.getId(),
                "Student accepted the resolution of " + c.getId());
        return c;
    }

    public Complaint reject(User actor, String complaintId, String reason) {
        Complaint c = requireOwnedBy(actor, complaintId);
        if (reason == null || reason.isBlank()) {
            throw new UnauthorizedException("A reason is required to reject a resolution");
        }
        c.addRemark(new Remark(actor.getId(), actor.getName(), actor.getRole().name(),
                "Rejected: " + reason.trim(), Instant.now()));
        c.moveTo(ComplaintStatus.IN_PROGRESS, actor.getName(), "Resolution rejected by complainant");
        store.saveComplaints();
        notifyUser(c.getAssignedToId(), c.getId(),
                "Student rejected the resolution of " + c.getId());
        return c;
    }

    public Complaint reopen(User actor, String complaintId) {
        Complaint c = requireOwnedBy(actor, complaintId);
        if (c.getReopenDeadlineAt() == null || Instant.now().isAfter(c.getReopenDeadlineAt())) {
            throw new UnauthorizedException("The reopen window for this complaint has passed");
        }
        c.setPriority(c.getPriority().bump());
        c.moveTo(ComplaintStatus.REOPENED, actor.getName(), "Reopened by complainant");
        c.setDeadlineAt(Instant.now().plus(autoCloseWindow));
        store.saveComplaints();
        notifyUser(c.getAssignedToId(), c.getId(),
                c.getId() + " reopened by the student");
        return c;
    }

    public void addRemark(User actor, String complaintId, String text) {
        if (text == null || text.isBlank()) {
            throw new UnauthorizedException("Remark cannot be empty");
        }
        Complaint c = require(complaintId);
        c.addRemark(new Remark(actor.getId(), actor.getName(), actor.getRole().name(),
                text.trim(), Instant.now()));
        store.saveComplaints();
        String counterpart = actor.getId().equals(c.getCreatedById())
                ? c.getAssignedToId()
                : c.getCreatedById();
        notifyUser(counterpart, c.getId(),
                "New remark on " + c.getId() + " by " + actor.getName());
    }

    public Optional<Complaint> findById(String id) {
        return store.getComplaints().stream().filter(c -> c.getId().equals(id)).findFirst();
    }

    public Complaint require(String id) {
        return findById(id).orElseThrow(() -> new UnauthorizedException("Complaint not found"));
    }

    public List<Complaint> all() {
        return sorted(store.getComplaints());
    }

    public List<Complaint> byUser(String userId) {
        return sorted(store.getComplaints().stream()
                .filter(c -> userId.equals(c.getCreatedById()))
                .toList());
    }

    public List<Complaint> byDepartment(String departmentUserId) {
        return sorted(store.getComplaints().stream()
                .filter(c -> departmentUserId.equals(c.getAssignedToId()))
                .toList());
    }

    public List<Complaint> search(String keyword, ComplaintStatus status, Category category) {
        String kw = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return sorted(store.getComplaints().stream()
                .filter(c -> kw.isEmpty()
                        || c.getTitle().toLowerCase(Locale.ROOT).contains(kw)
                        || c.getId().toLowerCase(Locale.ROOT).contains(kw)
                        || c.getDescription().toLowerCase(Locale.ROOT).contains(kw))
                .filter(c -> status == null || c.getStatus() == status)
                .filter(c -> category == null || c.getCategory() == category)
                .toList());
    }

    public long countByStatus(ComplaintStatus status) {
        return store.getComplaints().stream().filter(c -> c.getStatus() == status).count();
    }

    private Complaint requireOwnedBy(User actor, String complaintId) {
        Complaint c = require(complaintId);
        if (!actor.isStudent() || !c.getCreatedById().equals(actor.getId())) {
            throw new UnauthorizedException("Only the complainant can perform this action");
        }
        return c;
    }

    private Complaint requireAssignedTo(User actor, String complaintId) {
        Complaint c = require(complaintId);
        if (!actor.isDepartment() || !actor.getId().equals(c.getAssignedToId())) {
            throw new UnauthorizedException("This complaint is not assigned to you");
        }
        return c;
    }

    private void requireAdmin(User actor) {
        if (!actor.isAdmin()) {
            throw new UnauthorizedException("Only an admin can assign complaints");
        }
    }

    private void notifyUser(String userId, String complaintId, String message) {
        notifications.notifyUser(userId, complaintId, message);
    }

    private List<Complaint> sorted(List<Complaint> list) {
        return list.stream()
                .sorted(Comparator.comparing(Complaint::getUpdatedAt).reversed())
                .toList();
    }
}
