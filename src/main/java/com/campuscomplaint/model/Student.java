package com.campuscomplaint.model;

public class Student extends User {

    public Student() {
    }

    public Student(String id, String name, String email, String passwordHash) {
        super(id, name, email, passwordHash, Role.STUDENT);
    }

    @Override
    public String dashboardView() {
        return "/dashboard/student";
    }
}
