package com.project.skillswap.entity;

import jakarta.persistence.*;

import java.util.Locale;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // 'STUDENT' ya 'MENTOR'

    public static String normalizeRole(String role) {
        if (role == null) {
            return null;
        }

        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if ("M".equals(normalized)) {
            return "MENTOR";
        }
        if ("S".equals(normalized)) {
            return "STUDENT";
        }
        if ("A".equals(normalized)) {
            return "ADMIN";
        }
        return normalized;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return normalizeRole(role); }
    public void setRole(String role) { this.role = normalizeRole(role); }
}