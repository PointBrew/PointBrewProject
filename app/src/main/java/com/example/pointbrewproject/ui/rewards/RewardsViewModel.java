package com.example.pointbrewproject.ui.rewards;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.pointbrewproject.data.model.Reward;
import com.example.pointbrewproject.data.repository.RewardRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class RewardsViewModel extends ViewModel {
    private final RewardRepository rewardRepository;
    private final MutableLiveData<List<Reward>> availableRewards = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public RewardsViewModel() {
        rewardRepository = RewardRepository.getInstance();
        loadRewards();
    }

    private void loadRewards() {
        rewardRepository.getAllRewards(new RewardRepository.RewardCallback() {
            public void onComplete(List<Reward> rewards) {
                availableRewards.setValue(rewards);
            }

            public void onError(String error) {
                errorMessage.setValue(error);
            }
        });
    }

    public LiveData<List<Reward>> getAvailableRewards() {
        return availableRewards;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void redeemReward(Reward reward, RewardRepository.RedeemCallback callback) {
        rewardRepository.redeemReward(reward, (success, message) -> {
            if (success) {
                loadRewards(); // Reload rewards after successful redemption
            }
            callback.onRedeemComplete(success, message);
        });
    }

    public void filterRewards(String query) {
        List<Reward> currentRewards = availableRewards.getValue();
        if (currentRewards != null) {
            List<Reward> filteredRewards = new java.util.ArrayList<>();
            for (Reward reward : currentRewards) {
                if (reward.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    reward.getDescription().toLowerCase().contains(query.toLowerCase())) {
                    filteredRewards.add(reward);
                }
            }
            availableRewards.setValue(filteredRewards);
        }
    }

    public void resetFilter() {
        loadRewards();
    }
} 