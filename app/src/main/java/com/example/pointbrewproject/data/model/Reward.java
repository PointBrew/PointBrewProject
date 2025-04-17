package com.example.pointbrewproject.data.model;

public class Reward {
    private String id;
    private String title;
    private String description;
    private int pointsRequired;
    private String expirationDate;
    private String imageUrl;
    private boolean available;
    private String rewardType; // "POINTS", "DISCOUNT", "ITEM"
    private int discountPercentage;
    private String itemDetails;

    // Empty constructor for Firestore
    public Reward() {
        this.available = true;
        this.rewardType = "POINTS"; // Default type
    }

    public Reward(String title, String description, int pointsRequired, String expirationDate) {
        this.title = title;
        this.description = description;
        this.pointsRequired = pointsRequired;
        this.expirationDate = expirationDate;
        this.available = true;
        this.rewardType = "POINTS"; // Default type
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
    
    public String getRewardType() {
        return rewardType;
    }
    
    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
    }
    
    public int getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(int discountPercentage) {
        this.discountPercentage = discountPercentage;
    }
    
    public String getItemDetails() {
        return itemDetails;
    }
    
    public void setItemDetails(String itemDetails) {
        this.itemDetails = itemDetails;
    }
} 