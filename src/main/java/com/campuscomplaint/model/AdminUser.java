package com.campuscomplaint.model;

public class AdminUser extends User {

    public AdminUser() {
    }

    public AdminUser(String id, String name, String email, String passwordHash) {
        super(id, name, email, passwordHash, Role.ADMIN);
    }

    @Override
    public String dashboardView() {
        return "/dashboard/admin";
    }
}
