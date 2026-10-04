package com.campuscomplaint.model;

import java.time.Instant;

public class Remark {

    private String authorId;
    private String authorName;
    private String role;
    private String text;
    private Instant createdAt;

    public Remark() {
    }

    public Remark(String authorId, String authorName, String role, String text, Instant createdAt) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.role = role;
        this.text = text;
        this.createdAt = createdAt;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
