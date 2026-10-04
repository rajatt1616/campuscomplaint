package com.campuscomplaint.model;

import java.time.Instant;

public class StatusEvent {

    private ComplaintStatus from;
    private ComplaintStatus to;
    private String actorName;
    private String note;
    private Instant at;

    public StatusEvent() {
    }

    public StatusEvent(ComplaintStatus from, ComplaintStatus to, String actorName, String note, Instant at) {
        this.from = from;
        this.to = to;
        this.actorName = actorName;
        this.note = note;
        this.at = at;
    }

    public ComplaintStatus getFrom() {
        return from;
    }

    public void setFrom(ComplaintStatus from) {
        this.from = from;
    }

    public ComplaintStatus getTo() {
        return to;
    }

    public void setTo(ComplaintStatus to) {
        this.to = to;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getAt() {
        return at;
    }

    public void setAt(Instant at) {
        this.at = at;
    }
}
