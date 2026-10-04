package com.campuscomplaint.model;

public enum Priority {
    LOW(1, "Low"),
    MEDIUM(2, "Medium"),
    HIGH(3, "High"),
    URGENT(4, "Urgent");

    private final int rank;
    private final String label;

    Priority(int rank, String label) {
        this.rank = rank;
        this.label = label;
    }

    public int getRank() {
        return rank;
    }

    public String getLabel() {
        return label;
    }

    public Priority bump() {
        return rank < URGENT.rank ? Priority.values()[rank] : URGENT;
    }
}
