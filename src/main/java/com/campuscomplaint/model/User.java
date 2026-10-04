package com.campuscomplaint.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = Student.class, name = "STUDENT"),
        @JsonSubTypes.Type(value = AdminUser.class, name = "ADMIN"),
        @JsonSubTypes.Type(value = DepartmentUser.class, name = "DEPARTMENT")
})
public abstract class User {

    private String id;
    private String name;
    private String email;
    private String passwordHash;
    private Role role;

    protected User() {
    }

    protected User(String id, String name, String email, String passwordHash, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public abstract String dashboardView();

    @JsonIgnore
    public boolean isStudent() {
        return role == Role.STUDENT;
    }

    @JsonIgnore
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    @JsonIgnore
    public boolean isDepartment() {
        return role == Role.DEPARTMENT;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
