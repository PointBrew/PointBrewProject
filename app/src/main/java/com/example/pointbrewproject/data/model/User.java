package com.example.pointbrewproject.data.model;

public class User {
    private String id;
    private String email;
    private String name;
    private String displayName;
    private String profilePictureUrl;
    private String role;
    private int points;
    private boolean isAdmin;

    public User() {
        // Default constructor required for Firestore
    }

    public User(String id, String email, String name, String profilePictureUrl, String role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.displayName = name;
        this.profilePictureUrl = profilePictureUrl;
        this.role = role;
        this.points = 0;
        this.isAdmin = "admin".equals(role);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
        this.isAdmin = "admin".equals(role);
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
        this.role = admin ? "admin" : "user";
    }
} 