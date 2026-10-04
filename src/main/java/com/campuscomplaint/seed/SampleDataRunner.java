package com.campuscomplaint.seed;

import com.campuscomplaint.model.AdminUser;
import com.campuscomplaint.model.Category;
import com.campuscomplaint.model.Complaint;
import com.campuscomplaint.model.ComplaintStatus;
import com.campuscomplaint.model.DepartmentUser;
import com.campuscomplaint.model.Priority;
import com.campuscomplaint.model.Remark;
import com.campuscomplaint.model.Role;
import com.campuscomplaint.model.Student;
import com.campuscomplaint.model.User;
import com.campuscomplaint.service.AuthService;
import com.campuscomplaint.store.JsonDataStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@Order(1)
public class SampleDataRunner implements CommandLineRunner {

    private final AuthService auth;
    private final JsonDataStore store;

    public SampleDataRunner(AuthService auth, JsonDataStore store) {
        this.auth = auth;
        this.store = store;
    }

    @Override
    public void run(String... args) {
        auth.seedIfEmpty();
        if (!store.getComplaints().isEmpty()) {
            return;
        }
        Instant now = Instant.now();
        Duration window = Duration.ofDays(3);

        Student student = (Student) auth.findById("U-001").orElseThrow();
        AdminUser admin = (AdminUser) auth.findById("U-002").orElseThrow();
        DepartmentUser mess = (DepartmentUser) auth.findById("U-003").orElseThrow();
        DepartmentUser hostel = (DepartmentUser) auth.findById("U-004").orElseThrow();
        DepartmentUser academics = (DepartmentUser) auth.findById("U-005").orElseThrow();

        Complaint c1 = base("Cold food at breakfast every day",
                "Breakfast items at the mess are served cold since last week. Multiple students have complained informally but nothing has changed.",
                Category.MESS, Priority.MEDIUM, student, now.minus(Duration.ofDays(1)), window);
        store.getComplaints().add(c1);

        Complaint c2 = base("Hostel washroom light not working",
                "The second-floor washroom light in Block C has been out for three days. It is unsafe at night.",
                Category.HOSTEL, Priority.HIGH, student, now.minus(Duration.ofDays(2)), window);
        c2.setAssignedToId(hostel.getId());
        c2.setAssignedToName(hostel.getName());
        c2.moveTo(ComplaintStatus.ASSIGNED, admin.getName(), "Assigned to " + hostel.getName());
        store.getComplaints().add(c2);

        Complaint c3 = base("Lab machines too slow for DB practicals",
                "Computers in the DB lab take almost ten minutes to boot and Practical 4 cannot be completed in the allotted slot.",
                Category.ACADEMICS, Priority.MEDIUM, student, now.minus(Duration.ofDays(3)), window);
        assignAndAdvance(c3, academics, admin);
        store.getComplaints().add(c3);

        Complaint c4 = base("Broken desk in lecture hall L-204",
                "Second-row desk has a broken top and is unusable. Seat allocation forces students to share.",
                Category.INFRASTRUCTURE, Priority.LOW, student, now.minus(Duration.ofDays(4)), window);
        c4.setAssignedToId(academics.getId());
        c4.setAssignedToName(academics.getName());
        c4.moveTo(ComplaintStatus.ASSIGNED, admin.getName(), "Assigned to " + academics.getName());
        c4.moveTo(ComplaintStatus.IN_PROGRESS, academics.getName(), "Carpenter notified");
        c4.moveTo(ComplaintStatus.RESOLVED, academics.getName(), "Desk replaced");
        c4.addRemark(new Remark(academics.getId(), academics.getName(), Role.DEPARTMENT.name(),
                "New desk installed, please verify and accept.", now.minus(Duration.ofHours(20))));
        c4.setDeadlineAt(now.plus(window));
        store.getComplaints().add(c4);

        Complaint c5 = base("Duplicate charge in lab fee receipt",
                "Receipt shows lab fee charged twice for this semester. Attached scan available with the office.",
                Category.ACADEMICS, Priority.URGENT, student, now.minus(Duration.ofDays(6)), window);
        c5.setAssignedToId(academics.getId());
        c5.setAssignedToName(academics.getName());
        c5.moveTo(ComplaintStatus.ASSIGNED, admin.getName(), "Assigned to " + academics.getName());
        c5.moveTo(ComplaintStatus.IN_PROGRESS, academics.getName(), "Checked accounts");
        c5.moveTo(ComplaintStatus.RESOLVED, academics.getName(), "Refund initiated");
        c5.moveTo(ComplaintStatus.CLOSED, student.getName(), "Accepted by complainant");
        c5.setClosedAt(now.minus(Duration.ofHours(12)));
        c5.setReopenDeadlineAt(now.plus(Duration.ofDays(1)));
        c5.setDeadlineAt(null);
        store.getComplaints().add(c5);

        Complaint c6 = base("Water cooler on floor 3 not cooling",
                "The water cooler outside the reading room has stopped cooling since Monday.",
                Category.INFRASTRUCTURE, Priority.MEDIUM, student, now.minus(Duration.ofDays(2)), window);
        c6.setAssignedToId(hostel.getId());
        c6.setAssignedToName(hostel.getName());
        c6.moveTo(ComplaintStatus.ASSIGNED, admin.getName(), "Assigned to " + hostel.getName());
        c6.moveTo(ComplaintStatus.IN_PROGRESS, hostel.getName(), "Technician scheduled");
        c6.moveTo(ComplaintStatus.RESOLVED, hostel.getName(), "Filter replaced");
        c6.setPriority(c6.getPriority().bump());
        c6.moveTo(ComplaintStatus.CLOSED, student.getName(), "Accepted by complainant");
        c6.setClosedAt(now.minus(Duration.ofHours(30)));
        c6.setReopenDeadlineAt(now.plus(Duration.ofDays(1)));
        c6.moveTo(ComplaintStatus.REOPENED, student.getName(), "Issue recurred — reopened");
        c6.setDeadlineAt(now.plus(window));
        store.getComplaints().add(c6);

        store.saveComplaints();
    }

    private Complaint base(String title, String desc, Category category, Priority priority,
                           Student student, Instant createdAt, Duration window) {
        Complaint c = new Complaint();
        c.setId(store.nextComplaintId());
        c.setTitle(title);
        c.setDescription(desc);
        c.setCategory(category);
        c.setPriority(priority);
        c.setStatus(ComplaintStatus.FILED);
        c.setCreatedById(student.getId());
        c.setCreatedByName(student.getName());
        c.setCreatedAt(createdAt);
        c.setUpdatedAt(createdAt);
        c.setDeadlineAt(Instant.now().plus(window));
        return c;
    }

    private void assignAndAdvance(Complaint c, DepartmentUser dept, AdminUser admin) {
        c.setAssignedToId(dept.getId());
        c.setAssignedToName(dept.getName());
        c.moveTo(ComplaintStatus.ASSIGNED, admin.getName(), "Assigned to " + dept.getName());
        c.moveTo(ComplaintStatus.IN_PROGRESS, dept.getName(), "Under inspection");
    }
}
