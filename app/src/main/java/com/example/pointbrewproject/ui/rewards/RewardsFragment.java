package com.example.pointbrewproject.ui.rewards;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.Reward;
import com.example.pointbrewproject.data.model.User;
import com.example.pointbrewproject.data.repository.RewardRepository;
import com.example.pointbrewproject.data.repository.UserRepository;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class RewardsFragment extends Fragment implements RewardsAdapter.OnRewardClickListener {
    private RewardsViewModel viewModel;
    private TextView pointsValueText;
    private RecyclerView availableRewardsRecyclerView;
    private RecyclerView redeemedRewardsRecyclerView;
    private RecyclerView pointsHistoryRecyclerView;
    private RewardsAdapter availableRewardsAdapter;
    private UserRepository userRepository;
    private RewardRepository rewardRepository;
    private List<Reward> allRewards = new ArrayList<>();
    private MaterialButton earnPointsButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RewardsViewModel.class);
        userRepository = UserRepository.getInstance();
        rewardRepository = RewardRepository.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rewards, container, false);

        // Initialize views
        pointsValueText = view.findViewById(R.id.points_value);
        availableRewardsRecyclerView = view.findViewById(R.id.available_rewards_recycler);
        redeemedRewardsRecyclerView = view.findViewById(R.id.redeemed_rewards_recycler);
        pointsHistoryRecyclerView = view.findViewById(R.id.points_history_recycler);
        earnPointsButton = view.findViewById(R.id.earn_points_button);

        // Setup RecyclerView for available rewards
        availableRewardsAdapter = new RewardsAdapter(allRewards, this::onRewardRedeemed);
        availableRewardsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        availableRewardsRecyclerView.setAdapter(availableRewardsAdapter);

        // Load user points
        userRepository.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult();
                pointsValueText.setText(String.valueOf(user.getPoints()));
            }
        });

        // Load rewards
        rewardRepository.getAllRewards(rewards -> {
            allRewards = rewards;
            availableRewardsAdapter.updateRewards(rewards);
        });

        // Setup earn points button
        earnPointsButton.setOnClickListener(v -> {
            // Navigate to QR scanner or show QR code dialog
            // Implementation will go here
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                // Show error message to user
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRewardClick(Reward reward) {
        userRepository.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult();
                if (user.getPoints() >= reward.getPointsRequired()) {
                    // Show confirmation dialog
                    showRedeemConfirmationDialog(reward, user);
                } else {
                    Toast.makeText(requireContext(), "Not enough points to redeem this reward", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showRedeemConfirmationDialog(Reward reward, User user) {
        // Implement confirmation dialog
        // This is a placeholder for the actual implementation
        Toast.makeText(requireContext(), "Confirm redeeming " + reward.getTitle() + "?", Toast.LENGTH_SHORT).show();
    }

    private void onRewardRedeemed(Reward reward) {
        RewardRepository.getInstance().redeemReward(reward, (success, message) -> {
            if (success) {
                // Show success message
                Toast.makeText(requireContext(), "Reward redeemed successfully!", Toast.LENGTH_SHORT).show();
                
                // Refresh rewards lists
                refreshRewards();
            } else {
                // Show error message
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void refreshRewards() {
        // Reload rewards from repository
        rewardRepository.getAllRewards(rewards -> {
            if (rewards != null) {
                allRewards = rewards;
                availableRewardsAdapter.updateRewards(rewards);
            }
        });
        
        // Reload user data for updated points
        userRepository.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult();
                pointsValueText.setText(String.valueOf(user.getPoints()));
            }
        });
    }
} 