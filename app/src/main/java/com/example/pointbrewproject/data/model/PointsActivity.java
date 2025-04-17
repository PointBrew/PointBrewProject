package com.example.pointbrewproject.data.model;

import com.google.firebase.Timestamp;

/**
 * Model class representing a points activity (earning or spending) for a user
 */
public class PointsActivity {
    private String id;
    private String userId;
    private int points;
    private boolean isEarned; // true for earned points, false for spent/redeemed points
    private String description;
    private Timestamp timestamp;
    private String sourceId; // QR code ID or reward ID
    private String sourceType; // "QR_CODE", "REWARD_REDEMPTION", etc.

    // Empty constructor required for Firestore
    public PointsActivity() {
    }

    public PointsActivity(String userId, int points, boolean isEarned, String description, 
                         String sourceId, String sourceType) {
        this.userId = userId;
        this.points = points;
        this.isEarned = isEarned;
        this.description = description;
        this.sourceId = sourceId;
        this.sourceType = sourceType;
        this.timestamp = Timestamp.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public boolean isEarned() {
        return isEarned;
    }

    public void setEarned(boolean earned) {
        isEarned = earned;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
} 