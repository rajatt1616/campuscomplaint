package com.campuscomplaint.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DepartmentUser extends User {

    @JsonProperty("department")
    private String department;

    public DepartmentUser() {
    }

    public DepartmentUser(String id, String name, String email, String passwordHash, String department) {
        super(id, name, email, passwordHash, Role.DEPARTMENT);
        this.department = department;
    }

    @JsonProperty("department")
    public String getDepartment() {
        return department;
    }

    @JsonProperty("department")
    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String dashboardView() {
        return "/dashboard/department";
    }
}
