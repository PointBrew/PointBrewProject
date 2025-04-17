package com.example.pointbrewproject.data.model;

import com.google.firebase.Timestamp;

/**
 * Model class representing a reward that has been redeemed by a user
 */
public class RedeemedReward {
    private String id;
    private String rewardId;
    private String rewardTitle;
    private String rewardDescription;
    private String rewardType;
    private int pointsSpent;
    private Timestamp redeemedAt;
    private String userId;
    private String rewardImageUrl;

    // Empty constructor required for Firestore
    public RedeemedReward() {
    }

    public RedeemedReward(String rewardId, String rewardTitle, String rewardDescription, 
                           String rewardType, int pointsSpent, String userId) {
        this.rewardId = rewardId;
        this.rewardTitle = rewardTitle;
        this.rewardDescription = rewardDescription;
        this.rewardType = rewardType;
        this.pointsSpent = pointsSpent;
        this.userId = userId;
        this.redeemedAt = Timestamp.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRewardId() {
        return rewardId;
    }

    public void setRewardId(String rewardId) {
        this.rewardId = rewardId;
    }

    public String getRewardTitle() {
        return rewardTitle;
    }

    public void setRewardTitle(String rewardTitle) {
        this.rewardTitle = rewardTitle;
    }

    public String getRewardDescription() {
        return rewardDescription;
    }

    public void setRewardDescription(String rewardDescription) {
        this.rewardDescription = rewardDescription;
    }

    public String getRewardType() {
        return rewardType;
    }

    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
    }

    public int getPointsSpent() {
        return pointsSpent;
    }

    public void setPointsSpent(int pointsSpent) {
        this.pointsSpent = pointsSpent;
    }

    public Timestamp getRedeemedAt() {
        return redeemedAt;
    }

    public void setRedeemedAt(Timestamp redeemedAt) {
        this.redeemedAt = redeemedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRewardImageUrl() {
        return rewardImageUrl;
    }

    public void setRewardImageUrl(String rewardImageUrl) {
        this.rewardImageUrl = rewardImageUrl;
    }
} 