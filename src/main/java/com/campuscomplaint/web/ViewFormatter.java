package com.campuscomplaint.web;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ViewFormatter {

    private static final DateTimeFormatter FULL =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").withZone(ZoneId.of("Asia/Kolkata"));
    private static final DateTimeFormatter SHORT =
            DateTimeFormatter.ofPattern("dd MMM").withZone(ZoneId.of("Asia/Kolkata"));

    public String date(Instant instant) {
        return instant == null ? "—" : FULL.format(instant);
    }

    public String shortDate(Instant instant) {
        return instant == null ? "—" : SHORT.format(instant);
    }

    public String countdown(Instant instant) {
        if (instant == null) {
            return "—";
        }
        java.time.Duration d = java.time.Duration.between(java.time.Instant.now(), instant);
        boolean overdue = d.isNegative();
        d = d.abs();
        long days = d.toDays();
        long hours = d.toHours() % 24;
        long minutes = d.toMinutes() % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (days == 0 && hours == 0) {
            sb.append(minutes).append("m ");
        }
        String base = sb.toString().trim();
        if (base.isEmpty()) {
            base = "less than a minute";
        }
        return overdue ? "Overdue by " + base : base + " left";
    }

    public boolean isOverdue(Instant instant) {
        return instant != null && java.time.Instant.now().isAfter(instant);
    }

    public boolean isDueSoon(Instant instant) {
        if (instant == null || isOverdue(instant)) {
            return false;
        }
        return java.time.Duration.between(java.time.Instant.now(), instant)
                .compareTo(java.time.Duration.ofHours(24)) <= 0;
    }
}
