package com.example.pointbrewproject.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pointbrewproject.data.model.QRCode;
import com.example.pointbrewproject.data.model.Reward;
import com.example.pointbrewproject.data.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PointsRepository {
    private static final String TAG = "PointsRepository";
    private static PointsRepository instance;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;
    private final UserRepository userRepository;

    private final MutableLiveData<Integer> userPoints = new MutableLiveData<>(0);
    private final MutableLiveData<List<Reward>> availableRewards = new MutableLiveData<>(new ArrayList<>());

    private PointsRepository() {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        userRepository = UserRepository.getInstance();
        loadUserPoints();
        loadRewards();
    }

    public static synchronized PointsRepository getInstance() {
        if (instance == null) {
            instance = new PointsRepository();
        }
        return instance;
    }

    public LiveData<Integer> getUserPoints() {
        return userPoints;
    }

    public LiveData<List<Reward>> getAvailableRewards() {
        return availableRewards;
    }

    private void loadUserPoints() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            userPoints.setValue(0);
            return;
        }

        firestore.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("points")) {
                        Integer points = documentSnapshot.getLong("points") != null ?
                                documentSnapshot.getLong("points").intValue() : 0;
                        userPoints.setValue(points);
                    } else {
                        // Initialize points if not present
                        firestore.collection("users").document(currentUser.getUid())
                                .update("points", 0)
                                .addOnSuccessListener(aVoid -> userPoints.setValue(0))
                                .addOnFailureListener(e -> Log.e(TAG, "Error initializing points", e));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading user points", e));
    }

    private void loadRewards() {
        firestore.collection("rewards")
                .whereEqualTo("available", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Reward> rewards = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Reward reward = document.toObject(Reward.class);
                        reward.setId(document.getId());
                        rewards.add(reward);
                    }
                    availableRewards.setValue(rewards);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading rewards", e));
    }

    public void processQRCode(String qrCodeData, QRCodeCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onResult(false, "User not authenticated", 0);
            return;
        }

        // Look for the QR code in the database
        firestore.collection("qrcodes")
                .whereEqualTo("code", qrCodeData)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onResult(false, "Invalid QR code", 0);
                        return;
                    }

                    DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                    QRCode qrCode = document.toObject(QRCode.class);
                    
                    if (qrCode == null) {
                        callback.onResult(false, "Invalid QR code data", 0);
                        return;
                    }

                    // Check if the QR code is valid
                    if (!qrCode.isValid()) {
                        callback.onResult(false, "QR code is no longer valid", 0);
                        return;
                    }

                    // For one-time QR codes, check if user has already used it
                    if (qrCode.isOneTime()) {
                        firestore.collection("users")
                                .document(currentUser.getUid())
                                .collection("scannedQRCodes")
                                .document(qrCode.getId())
                                .get()
                                .addOnSuccessListener(scannedDoc -> {
                                    if (scannedDoc.exists()) {
                                        callback.onResult(false, "You've already used this QR code", 0);
                                    } else {
                                        addPoints(currentUser.getUid(), qrCode, callback);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error checking scanned QR codes", e);
                                    callback.onResult(false, "Error checking QR code history", 0);
                                });
                    } else {
                        // QR code can be used multiple times
                        addPoints(currentUser.getUid(), qrCode, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error querying QR code", e);
                    callback.onResult(false, "Error processing QR code", 0);
                });
    }

    private void addPoints(String userId, QRCode qrCode, QRCodeCallback callback) {
        firestore.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentReference userRef = firestore.collection("users").document(userId);
            DocumentSnapshot userSnapshot = transaction.get(userRef);
            
            // Get current points or initialize to 0
            long currentPoints = 0;
            if (userSnapshot.contains("points")) {
                currentPoints = userSnapshot.getLong("points") != null ? 
                        userSnapshot.getLong("points") : 0;
            }
            
            // Add points from QR code
            long newPoints = currentPoints + qrCode.getPointsValue();
            
            // Update user points
            transaction.update(userRef, "points", newPoints);
            
            // If one-time use, mark as scanned
            if (qrCode.isOneTime()) {
                // Record that user has scanned this QR code
                Map<String, Object> scannedData = new HashMap<>();
                scannedData.put("timestamp", com.google.firebase.Timestamp.now());
                scannedData.put("pointsAwarded", qrCode.getPointsValue());
                
                transaction.set(
                        userRef.collection("scannedQRCodes").document(qrCode.getId()),
                        scannedData
                );
            }
            
            return null;
        }).addOnSuccessListener(aVoid -> {
            // Update cached points value
            if (userPoints.getValue() != null) {
                userPoints.setValue(userPoints.getValue() + qrCode.getPointsValue());
            } else {
                loadUserPoints(); // Reload if null
            }
            callback.onResult(true, "Successfully added " + qrCode.getPointsValue() + " points!", qrCode.getPointsValue());
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Transaction failure", e);
            callback.onResult(false, "Failed to add points", 0);
        });
    }

    public void redeemReward(Reward reward, RewardCallback callback) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            callback.onResult(false, "User not authenticated");
            return;
        }

        // Check if user has enough points
        Integer currentPoints = userPoints.getValue();
        if (currentPoints == null || currentPoints < reward.getPointsRequired()) {
            callback.onResult(false, "Not enough points to redeem this reward");
            return;
        }

        firestore.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentReference userRef = firestore.collection("users").document(currentUser.getUid());
            DocumentSnapshot userSnapshot = transaction.get(userRef);
            
            // Verify points again in transaction
            Long pointsLong = userSnapshot.getLong("points");
            long points = pointsLong != null ? pointsLong : 0;
            
            if (points < reward.getPointsRequired()) {
                throw new RuntimeException("Not enough points");
            }
            
            // Deduct points
            long newPoints = points - reward.getPointsRequired();
            transaction.update(userRef, "points", newPoints);
            
            // Record redemption
            Map<String, Object> redemptionData = new HashMap<>();
            redemptionData.put("rewardId", reward.getId());
            redemptionData.put("rewardTitle", reward.getTitle());
            redemptionData.put("pointsSpent", reward.getPointsRequired());
            redemptionData.put("timestamp", com.google.firebase.Timestamp.now());
            
            transaction.set(
                    userRef.collection("redeemedRewards").document(),
                    redemptionData
            );
            
            return null;
        }).addOnSuccessListener(aVoid -> {
            // Update cached points
            if (userPoints.getValue() != null) {
                userPoints.setValue(userPoints.getValue() - reward.getPointsRequired());
            }
            callback.onResult(true, "Reward redeemed successfully!");
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Transaction failure", e);
            callback.onResult(false, "Failed to redeem reward: " + e.getMessage());
        });
    }

    public interface QRCodeCallback {
        void onResult(boolean success, String message, int pointsAdded);
    }

    public interface RewardCallback {
        void onResult(boolean success, String message);
    }
} 