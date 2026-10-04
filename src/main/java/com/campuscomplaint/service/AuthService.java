package com.campuscomplaint.service;

import com.campuscomplaint.exception.UnauthorizedException;
import com.campuscomplaint.model.AdminUser;
import com.campuscomplaint.model.DepartmentUser;
import com.campuscomplaint.model.Role;
import com.campuscomplaint.model.Student;
import com.campuscomplaint.model.User;
import com.campuscomplaint.store.JsonDataStore;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    private final JsonDataStore store;

    public AuthService(JsonDataStore store) {
        this.store = store;
    }

    public User login(String email, String password) {
        return findByEmail(email)
                .filter(u -> u.getPasswordHash().equals(hash(password)))
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
    }

    public Optional<User> findByEmail(String email) {
        return store.getUsers().stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public Optional<User> findById(String id) {
        return store.getUsers().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    public User requireById(String id) {
        return findById(id).orElseThrow(() -> new UnauthorizedException("Unknown user"));
    }

    public List<User> departments() {
        return store.getUsers().stream()
                .filter(User::isDepartment)
                .toList();
    }

    public void seedIfEmpty() {
        if (!store.getUsers().isEmpty()) {
            return;
        }
        store.getUsers().add(new Student("U-001", "Rajat Tyagi", "rajat@student.srmist.edu",
                hash("student123")));
        store.getUsers().add(new AdminUser("U-002", "Registrar Office", "admin@srmist.edu",
                hash("admin123")));
        store.getUsers().add(new DepartmentUser("U-003", "Mess Authority", "mess@srmist.edu",
                hash("dept123"), "Mess"));
        store.getUsers().add(new DepartmentUser("U-004", "Hostel Warden", "hostel@srmist.edu",
                hash("dept123"), "Hostel"));
        store.getUsers().add(new DepartmentUser("U-005", "Academic Office", "academics@srmist.edu",
                hash("dept123"), "Academics"));
        store.saveUsers();
    }

    public static String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public static boolean hasRole(User user, Role role) {
        return user.getRole() == role;
    }
}
