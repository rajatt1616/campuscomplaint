package com.campuscomplaint.model;

public enum Category {
    MESS("Mess"),
    HOSTEL("Hostel"),
    ACADEMICS("Academics"),
    INFRASTRUCTURE("Infrastructure"),
    OTHERS("Others");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
