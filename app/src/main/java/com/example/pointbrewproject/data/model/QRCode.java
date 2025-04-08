package com.example.pointbrewproject.data.model;

import java.util.Date;

public class QRCode {
    private String id;
    private String code;
    private int points;
    private int maxScans;
    private int currentScans;
    private Date expirationDate;
    private Date createdAt;
    private String createdBy;
    private boolean isActive;

    public QRCode() {
        // Default constructor required for Firestore
    }

    public QRCode(String code, int points, int maxScans, Date expirationDate, String createdBy) {
        this.code = code;
        this.points = points;
        this.maxScans = maxScans;
        this.currentScans = 0;
        this.expirationDate = expirationDate;
        this.createdAt = new Date();
        this.createdBy = createdBy;
        this.isActive = true;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getMaxScans() {
        return maxScans;
    }

    public void setMaxScans(int maxScans) {
        this.maxScans = maxScans;
    }

    public int getCurrentScans() {
        return currentScans;
    }

    public void setCurrentScans(int currentScans) {
        this.currentScans = currentScans;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isValid() {
        Date now = new Date();
        return isActive && 
               currentScans < maxScans && 
               now.before(expirationDate);
    }
    
    /**
     * Checks if this QR code can only be used once per user
     * @return true if the QR code is one-time use per user
     */
    public boolean isOneTime() {
        return maxScans == 1;
    }
    
    /**
     * Gets the point value of this QR code
     * @return the number of points this QR code is worth
     */
    public int getPointsValue() {
        return points;
    }
} 