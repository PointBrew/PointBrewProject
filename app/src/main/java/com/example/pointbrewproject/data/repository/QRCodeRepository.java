package com.example.pointbrewproject.data.repository;

import com.example.pointbrewproject.data.model.QRCode;
import com.example.pointbrewproject.data.model.PointsActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class QRCodeRepository {
    private static QRCodeRepository instance;
    private final CollectionReference qrCodesCollection;
    private final FirebaseFirestore db;

    private QRCodeRepository() {
        db = FirebaseFirestore.getInstance();
        qrCodesCollection = db.collection("qrcodes");
    }

    public static QRCodeRepository getInstance() {
        if (instance == null) {
            instance = new QRCodeRepository();
        }
        return instance;
    }

    public interface QRCodeCallback {
        void onComplete(List<QRCode> qrCodes);
    }

    public interface QRCodeOperationCallback {
        void onComplete(boolean success, String message);
    }

    public void generateQRCode(int points, int maxScans, long expirationDateMillis, String createdBy, QRCodeOperationCallback callback) {
        String code = UUID.randomUUID().toString();
        QRCode qrCode = new QRCode(code, points, maxScans, new Date(expirationDateMillis), createdBy);
        
        qrCodesCollection.add(qrCode)
            .addOnSuccessListener(documentReference -> {
                qrCode.setId(documentReference.getId());
                callback.onComplete(true, "QR code generated successfully");
            })
            .addOnFailureListener(e -> callback.onComplete(false, "Failed to generate QR code: " + e.getMessage()));
    }

    public void getAllQRCodes(QRCodeCallback callback) {
        qrCodesCollection.get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<QRCode> qrCodes = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        QRCode qrCode = document.toObject(QRCode.class);
                        qrCode.setId(document.getId());
                        qrCodes.add(qrCode);
                    }
                    callback.onComplete(qrCodes);
                } else {
                    callback.onComplete(new ArrayList<>());
                }
            });
    }

    public void updateQRCode(QRCode qrCode, QRCodeOperationCallback callback) {
        qrCodesCollection.document(qrCode.getId())
            .set(qrCode)
            .addOnSuccessListener(aVoid -> callback.onComplete(true, "QR code updated successfully"))
            .addOnFailureListener(e -> callback.onComplete(false, "Failed to update QR code: " + e.getMessage()));
    }

    public void deleteQRCode(String qrCodeId, QRCodeOperationCallback callback) {
        qrCodesCollection.document(qrCodeId)
            .delete()
            .addOnSuccessListener(aVoid -> callback.onComplete(true, "QR code deleted successfully"))
            .addOnFailureListener(e -> callback.onComplete(false, "Failed to delete QR code: " + e.getMessage()));
    }

    /**
     * Process a QR code, either scanned or manually entered
     * @param code The QR code value
     * @param callback Callback to notify about success or failure
     */
    public void processQRCode(String code, QRCodeOperationCallback callback) {
        // Get the current user
        UserRepository userRepository = UserRepository.getInstance();
        if (!userRepository.isLoggedIn()) {
            callback.onComplete(false, "You must be logged in to redeem a code");
            return;
        }
        
        userRepository.getCurrentUser(userTask -> {
            if (userTask.isSuccessful() && userTask.getResult() != null) {
                // Get the user's ID
                String userId = userTask.getResult().getId();
                
                // Search for the QR code in the database
                qrCodesCollection.whereEqualTo("code", code)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                            QRCode qrCode = document.toObject(QRCode.class);
                            qrCode.setId(document.getId());

                            // Check if the code is still valid
                            if (qrCode.getCurrentScans() >= qrCode.getMaxScans()) {
                                callback.onComplete(false, "This code has reached its maximum number of uses");
                                return;
                            }
                            
                            // Check if the code is expired
                            if (qrCode.getExpirationDate().before(new Date())) {
                                callback.onComplete(false, "This code has expired");
                                return;
                            }

                            // Update scan count
                            qrCode.setCurrentScans(qrCode.getCurrentScans() + 1);
                            
                            // Check if this was the last scan
                            boolean isLastScan = qrCode.getCurrentScans() >= qrCode.getMaxScans();
                            
                            // Update QR code in database
                            updateQRCode(qrCode, (success, message) -> {
                                if (success) {
                                    // Record the points activity
                                    PointsActivity pointsActivity = new PointsActivity(
                                            userId,
                                            qrCode.getPoints(),
                                            true,
                                            "Earned points from QR code",
                                            qrCode.getId(),
                                            "QR_CODE"
                                    );
                                    
                                    // Save the points activity
                                    FirebaseFirestore.getInstance()
                                            .collection("pointsActivities")
                                            .add(pointsActivity)
                                            .addOnSuccessListener(documentReference -> {
                                                // Add points to user
                                                int pointsEarned = qrCode.getPoints();
                                                userRepository.addPoints(userId, pointsEarned, (addSuccess, addMessage) -> {
                                                    if (addSuccess) {
                                                        String successMessage = "You earned " + pointsEarned + " points!";
                                                        if (isLastScan) {
                                                            successMessage += " (This was the last use of this code)";
                                                        }
                                                        callback.onComplete(true, successMessage);
                                                    } else {
                                                        callback.onComplete(false, addMessage);
                                                    }
                                                });
                                            })
                                            .addOnFailureListener(e -> {
                                                // Still add points even if activity recording fails
                                                userRepository.addPoints(userId, qrCode.getPoints(), callback);
                                            });
                                } else {
                                    callback.onComplete(false, message);
                                }
                            });
                        } else {
                            callback.onComplete(false, "Invalid code. Please try again.");
                        }
                    })
                    .addOnFailureListener(e -> callback.onComplete(false, "Error processing code: " + e.getMessage()));
            } else {
                callback.onComplete(false, "Failed to get user information");
            }
        });
    }

    public void scanQRCode(String code, String userId, QRCodeOperationCallback callback) {
        qrCodesCollection.whereEqualTo("code", code)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    QueryDocumentSnapshot document = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                    QRCode qrCode = document.toObject(QRCode.class);
                    qrCode.setId(document.getId());

                    if (!qrCode.isValid()) {
                        callback.onComplete(false, "QR code is no longer valid");
                        return;
                    }

                    // Update scan count
                    qrCode.setCurrentScans(qrCode.getCurrentScans() + 1);
                    if (qrCode.getCurrentScans() >= qrCode.getMaxScans()) {
                        qrCode.setActive(false);
                    }

                    // Update QR code in database
                    updateQRCode(qrCode, (success, message) -> {
                        if (success) {
                            // Record the points activity
                            PointsActivity pointsActivity = new PointsActivity(
                                    userId,
                                    qrCode.getPoints(),
                                    true,
                                    "Earned points from QR code",
                                    qrCode.getId(),
                                    "QR_CODE"
                            );
                            
                            // Save the points activity
                            FirebaseFirestore.getInstance()
                                    .collection("pointsActivities")
                                    .add(pointsActivity)
                                    .addOnSuccessListener(documentReference -> {
                                        // Add points to user
                                        UserRepository.getInstance().addPoints(userId, qrCode.getPoints(), callback);
                                    })
                                    .addOnFailureListener(e -> {
                                        // Still add points even if activity recording fails
                                        UserRepository.getInstance().addPoints(userId, qrCode.getPoints(), callback);
                                    });
                        } else {
                            callback.onComplete(false, message);
                        }
                    });
                } else {
                    callback.onComplete(false, "QR code not found");
                }
            })
            .addOnFailureListener(e -> callback.onComplete(false, "Failed to scan QR code: " + e.getMessage()));
    }
} 