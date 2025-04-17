package com.example.pointbrewproject.data.repository;

import com.example.pointbrewproject.data.model.PointsActivity;
import com.example.pointbrewproject.data.model.RedeemedReward;
import com.example.pointbrewproject.data.model.Reward;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;

public class RewardRepository {
    private static RewardRepository instance;
    private final CollectionReference rewardsCollection;
    private final CollectionReference redeemedRewardsCollection;
    private final CollectionReference pointsActivitiesCollection;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private RewardRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        rewardsCollection = db.collection("rewards");
        redeemedRewardsCollection = db.collection("redeemedRewards");
        pointsActivitiesCollection = db.collection("pointsActivities");
    }

    public static RewardRepository getInstance() {
        if (instance == null) {
            instance = new RewardRepository();
        }
        return instance;
    }

    public interface RewardCallback {
        void onComplete(List<Reward> rewards);
    }

    public interface RedeemedRewardCallback {
        void onComplete(List<RedeemedReward> rewards);
    }

    public interface PointsActivityCallback {
        void onComplete(List<PointsActivity> activities);
    }

    public interface RedeemCallback {
        void onRedeemComplete(boolean success, String message);
    }

    public void getAllRewards(RewardCallback callback) {
        rewardsCollection
                .whereEqualTo("available", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Reward> rewards = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            Reward reward = document.toObject(Reward.class);
                            if (reward != null) {
                                reward.setId(document.getId());
                                rewards.add(reward);
                            }
                        }
                        callback.onComplete(rewards);
                    } else {
                        callback.onComplete(new ArrayList<>());
                    }
                });
    }

    public void getRedeemedRewards(RedeemedRewardCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onComplete(new ArrayList<>());
            return;
        }

        redeemedRewardsCollection
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("redeemedAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<RedeemedReward> rewards = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            RedeemedReward reward = document.toObject(RedeemedReward.class);
                            if (reward != null) {
                                reward.setId(document.getId());
                                rewards.add(reward);
                            }
                        }
                        callback.onComplete(rewards);
                    } else {
                        callback.onComplete(new ArrayList<>());
                    }
                });
    }

    public void getPointsActivities(PointsActivityCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onComplete(new ArrayList<>());
            return;
        }

        pointsActivitiesCollection
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<PointsActivity> activities = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult()) {
                            PointsActivity activity = document.toObject(PointsActivity.class);
                            if (activity != null) {
                                activity.setId(document.getId());
                                activities.add(activity);
                            }
                        }
                        callback.onComplete(activities);
                    } else {
                        callback.onComplete(new ArrayList<>());
                    }
                });
    }

    public void redeemReward(Reward reward, RedeemCallback callback) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            callback.onRedeemComplete(false, "User not authenticated");
            return;
        }

        final String userId = currentUser.getUid();
        final UserRepository userRepository = UserRepository.getInstance();

        // Get current user to check points
        userRepository.getCurrentUser(userTask -> {
            if (userTask.isSuccessful() && userTask.getResult() != null) {
                int userPoints = userTask.getResult().getPoints();
                
                // Check if user has enough points
                if (userPoints < reward.getPointsRequired()) {
                    callback.onRedeemComplete(false, "Not enough points to redeem this reward");
                    return;
                }
                
                // Run transaction to ensure consistency
                db.runTransaction((Transaction.Function<Void>) transaction -> {
                    // Check if the reward is still available
                    DocumentReference rewardRef = rewardsCollection.document(reward.getId());
                    DocumentSnapshot rewardSnapshot = transaction.get(rewardRef);
                    
                    if (!rewardSnapshot.exists()) {
                        throw new RuntimeException("Reward no longer exists");
                    }
                    
                    Reward currentReward = rewardSnapshot.toObject(Reward.class);
                    if (currentReward == null || !currentReward.isAvailable()) {
                        throw new RuntimeException("Reward is no longer available");
                    }
                    
                    // Update user points
                    DocumentReference userRef = db.collection("users").document(userId);
                    DocumentSnapshot userSnapshot = transaction.get(userRef);
                    
                    if (!userSnapshot.exists()) {
                        throw new RuntimeException("User data not found");
                    }
                    
                    Long currentPoints = userSnapshot.getLong("points");
                    if (currentPoints == null || currentPoints < reward.getPointsRequired()) {
                        throw new RuntimeException("Not enough points to redeem this reward");
                    }
                    
                    long newPoints = currentPoints - reward.getPointsRequired();
                    transaction.update(userRef, "points", newPoints);
                    
                    // Create a redeemed reward record
                    RedeemedReward redeemedReward = new RedeemedReward(
                            reward.getId(),
                            reward.getTitle(),
                            reward.getDescription(),
                            reward.getRewardType(),
                            reward.getPointsRequired(),
                            userId
                    );
                    redeemedReward.setRewardImageUrl(reward.getImageUrl());
                    
                    // Create points activity record
                    PointsActivity pointsActivity = new PointsActivity(
                            userId,
                            -reward.getPointsRequired(), // Negative because points are spent
                            false,
                            "Redeemed reward: " + reward.getTitle(),
                            reward.getId(),
                            "REWARD_REDEMPTION"
                    );
                    
                    // Add records to Firestore
                    DocumentReference redeemedRewardRef = redeemedRewardsCollection.document();
                    DocumentReference pointsActivityRef = pointsActivitiesCollection.document();
                    
                    transaction.set(redeemedRewardRef, redeemedReward);
                    transaction.set(pointsActivityRef, pointsActivity);
                    
                    return null;
                })
                .addOnSuccessListener(aVoid -> callback.onRedeemComplete(true, "Reward redeemed successfully!"))
                .addOnFailureListener(e -> callback.onRedeemComplete(false, "Failed to redeem reward: " + e.getMessage()));
            } else {
                callback.onRedeemComplete(false, "Failed to get user data");
            }
        });
    }

    public void addReward(Reward reward, RewardCallback callback) {
        rewardsCollection.add(reward)
                .addOnSuccessListener(documentReference -> {
                    reward.setId(documentReference.getId());
                    getAllRewards(callback);
                })
                .addOnFailureListener(e -> callback.onComplete(new ArrayList<>()));
    }

    public void updateReward(Reward reward, RewardCallback callback) {
        rewardsCollection.document(reward.getId())
                .set(reward)
                .addOnSuccessListener(aVoid -> getAllRewards(callback))
                .addOnFailureListener(e -> callback.onComplete(new ArrayList<>()));
    }

    public void deleteReward(String rewardId, RewardCallback callback) {
        rewardsCollection.document(rewardId)
                .delete()
                .addOnSuccessListener(aVoid -> getAllRewards(callback))
                .addOnFailureListener(e -> callback.onComplete(new ArrayList<>()));
    }
} 