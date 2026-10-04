package com.campuscomplaint.model;

import java.util.EnumSet;
import java.util.Set;

public enum ComplaintStatus {

    FILED("Filed"),
    ASSIGNED("Assigned"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CLOSED("Closed"),
    AUTO_CLOSED("Auto Closed"),
    REOPENED("Reopened");

    private final String label;

    ComplaintStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean canTransitionTo(ComplaintStatus target) {
        return allowedTransitions().contains(target);
    }

    private Set<ComplaintStatus> allowedTransitions() {
        switch (this) {
            case FILED:
                return EnumSet.of(ASSIGNED, AUTO_CLOSED);
            case ASSIGNED:
                return EnumSet.of(IN_PROGRESS, AUTO_CLOSED);
            case IN_PROGRESS:
                return EnumSet.of(RESOLVED, AUTO_CLOSED);
            case RESOLVED:
                return EnumSet.of(CLOSED, IN_PROGRESS, AUTO_CLOSED);
            case CLOSED:
                return EnumSet.of(REOPENED);
            case AUTO_CLOSED:
                return EnumSet.of(REOPENED);
            case REOPENED:
                return EnumSet.of(IN_PROGRESS, AUTO_CLOSED);
            default:
                return EnumSet.noneOf(ComplaintStatus.class);
        }
    }
}
