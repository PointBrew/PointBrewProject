package com.example.pointbrewproject.data.repository;

import com.example.pointbrewproject.data.model.Reward;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class RewardRepository {
    private static RewardRepository instance;
    private final CollectionReference rewardsCollection;
    private final FirebaseFirestore db;

    private RewardRepository() {
        db = FirebaseFirestore.getInstance();
        rewardsCollection = db.collection("rewards");
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

    public interface RedeemCallback {
        void onRedeemComplete(boolean success, String message);
    }

    public void getAllRewards(RewardCallback callback) {
        rewardsCollection.get()
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

    public void redeemReward(Reward reward, RedeemCallback callback) {
        // First check if the reward is still available
        rewardsCollection.document(reward.getId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Reward currentReward = documentSnapshot.toObject(Reward.class);
                        if (currentReward != null && currentReward.isAvailable()) {
                            // Update reward availability
                            rewardsCollection.document(reward.getId())
                                    .update("available", false)
                                    .addOnSuccessListener(aVoid -> callback.onRedeemComplete(true, "Reward redeemed successfully!"))
                                    .addOnFailureListener(e -> callback.onRedeemComplete(false, "Failed to redeem reward: " + e.getMessage()));
                        } else {
                            callback.onRedeemComplete(false, "Reward is no longer available");
                        }
                    } else {
                        callback.onRedeemComplete(false, "Reward not found");
                    }
                })
                .addOnFailureListener(e -> callback.onRedeemComplete(false, "Failed to check reward availability: " + e.getMessage()));
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