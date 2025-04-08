package com.example.pointbrewproject.ui.points;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.pointbrewproject.data.model.User;
import com.example.pointbrewproject.data.repository.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import androidx.annotation.NonNull;

public class PointsViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<Integer> userPoints = new MutableLiveData<>(0);
    private final MutableLiveData<String> scanResult = new MutableLiveData<>("");
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public PointsViewModel() {
        userRepository = UserRepository.getInstance();
        loadUserData();
    }

    private void loadUserData() {
        userRepository.getCurrentUser(new OnCompleteListener<User>() {
            @Override
            public void onComplete(@NonNull Task<User> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    User user = task.getResult();
                    userPoints.setValue(user.getPoints());
                } else {
                    errorMessage.setValue("Failed to load user data");
                }
            }
        });
    }

    public LiveData<Integer> getUserPoints() {
        return userPoints;
    }

    public LiveData<String> getScanResult() {
        return scanResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void updateUserPoints(int newPoints) {
        userRepository.getCurrentUser(new OnCompleteListener<User>() {
            @Override
            public void onComplete(@NonNull Task<User> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    User user = task.getResult();
                    user.setPoints(newPoints);
                    userRepository.updateUser(user, new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(@NonNull Task<Boolean> updateTask) {
                            if (updateTask.isSuccessful() && Boolean.TRUE.equals(updateTask.getResult())) {
                                userPoints.setValue(newPoints);
                            } else {
                                errorMessage.setValue("Failed to update points");
                            }
                        }
                    });
                } else {
                    errorMessage.setValue("Failed to get current user");
                }
            }
        });
    }

    public void processQRCode(String qrCodeData) {
        if (qrCodeData == null || qrCodeData.isEmpty()) {
            scanResult.setValue("Invalid QR code");
            return;
        }

        // Process the QR code and add points
        // This is a placeholder - implement your actual logic
        int pointsToAdd = 100; // Example points
        addPoints(pointsToAdd, "QR Code: " + qrCodeData);
    }

    public void processManualCode(String code) {
        if (code == null || code.isEmpty()) {
            scanResult.setValue("Invalid code");
            return;
        }

        // Process the manual code and add points
        // This is a placeholder - implement your actual logic
        int pointsToAdd = 50; // Example points
        addPoints(pointsToAdd, "Manual Code: " + code);
    }

    private void addPoints(int points, String source) {
        Integer currentPoints = userPoints.getValue();
        if (currentPoints != null) {
            int newPoints = currentPoints + points;
            updateUserPoints(newPoints);
            scanResult.setValue("Added " + points + " points from " + source);
        }
    }
} 