package com.campuscomplaint.model;

import com.campuscomplaint.exception.InvalidStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Complaint {

    private String id;
    private String title;
    private String description;
    private Category category;
    private Priority priority;
    private ComplaintStatus status;
    private String createdById;
    private String createdByName;
    private String assignedToId;
    private String assignedToName;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deadlineAt;
    private Instant closedAt;
    private Instant reopenDeadlineAt;
    private List<Remark> remarks = new ArrayList<>();
    private List<StatusEvent> history = new ArrayList<>();

    public Complaint() {
    }

    public void moveTo(ComplaintStatus target, String actorName, String note) {
        if (target == status) {
            throw new InvalidStatusException("Complaint is already in " + target.getLabel());
        }
        if (!status.canTransitionTo(target)) {
            throw new InvalidStatusException(
                    "Cannot move from " + status.getLabel() + " to " + target.getLabel());
        }
        Instant now = Instant.now();
        history.add(new StatusEvent(status, target, actorName, note, now));
        status = target;
        updatedAt = now;
    }

    public void addRemark(Remark remark) {
        remarks.add(remark);
        updatedAt = remark.getCreatedAt();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public String getCreatedById() {
        return createdById;
    }

    public void setCreatedById(String createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(String assignedToId) {
        this.assignedToId = assignedToId;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDeadlineAt() {
        return deadlineAt;
    }

    public void setDeadlineAt(Instant deadlineAt) {
        this.deadlineAt = deadlineAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public Instant getReopenDeadlineAt() {
        return reopenDeadlineAt;
    }

    public void setReopenDeadlineAt(Instant reopenDeadlineAt) {
        this.reopenDeadlineAt = reopenDeadlineAt;
    }

    public List<Remark> getRemarks() {
        return remarks;
    }

    public void setRemarks(List<Remark> remarks) {
        this.remarks = remarks;
    }

    public List<StatusEvent> getHistory() {
        return history;
    }

    public void setHistory(List<StatusEvent> history) {
        this.history = history;
    }
}
