package com.example.pointbrewproject.data.model;

public class Reward {
    private String id;
    private String title;
    private String description;
    private int pointsRequired;
    private String expirationDate;
    private String imageUrl;
    private boolean available;

    // Empty constructor for Firestore
    public Reward() {
    }

    public Reward(String title, String description, int pointsRequired, String expirationDate) {
        this.title = title;
        this.description = description;
        this.pointsRequired = pointsRequired;
        this.expirationDate = expirationDate;
        this.available = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPointsRequired() {
        return pointsRequired;
    }

    public void setPointsRequired(int pointsRequired) {
        this.pointsRequired = pointsRequired;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
} 