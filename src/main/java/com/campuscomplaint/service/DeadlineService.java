package com.campuscomplaint.service;

import com.campuscomplaint.model.Complaint;
import com.campuscomplaint.model.ComplaintStatus;
import com.campuscomplaint.store.JsonDataStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

@Service
public class DeadlineService {

    private static final Set<ComplaintStatus> EXPIRABLE = EnumSet.of(
            ComplaintStatus.FILED,
            ComplaintStatus.ASSIGNED,
            ComplaintStatus.IN_PROGRESS,
            ComplaintStatus.RESOLVED,
            ComplaintStatus.REOPENED);

    private final JsonDataStore store;
    private final NotificationService notifications;

    public DeadlineService(JsonDataStore store, NotificationService notifications) {
        this.store = store;
        this.notifications = notifications;
    }

    @Scheduled(fixedDelayString = "${app.deadline.check-interval}")
    public void sweep() {
        boolean changed = false;
        Instant now = Instant.now();
        for (Complaint c : store.getComplaints()) {
            if (EXPIRABLE.contains(c.getStatus())
                    && c.getDeadlineAt() != null
                    && now.isAfter(c.getDeadlineAt())) {
                c.moveTo(ComplaintStatus.AUTO_CLOSED, "System", "Deadline passed — auto closed");
                c.setClosedAt(now);
                c.setReopenDeadlineAt(now.plus(java.time.Duration.ofDays(2)));
                notifications.notifyUser(c.getCreatedById(), c.getId(),
                        c.getId() + " was auto-closed — deadline passed");
                notifications.notifyUser(c.getAssignedToId(), c.getId(),
                        c.getId() + " was auto-closed — deadline passed");
                changed = true;
            }
        }
        if (changed) {
            store.saveComplaints();
        }
    }
}
