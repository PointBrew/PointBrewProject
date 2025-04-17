package com.example.pointbrewproject.ui.rewards;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.RedeemedReward;
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
    private TextView noRedeemedRewardsText;
    private RewardsAdapter availableRewardsAdapter;
    private RedeemedRewardsAdapter redeemedRewardsAdapter;
    private UserRepository userRepository;
    private RewardRepository rewardRepository;
    private List<Reward> availableRewards = new ArrayList<>();
    private List<RedeemedReward> redeemedRewards = new ArrayList<>();
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
        noRedeemedRewardsText = view.findViewById(R.id.no_redeemed_rewards_text);
        earnPointsButton = view.findViewById(R.id.earn_points_button);

        // Setup RecyclerView for available rewards
        availableRewardsAdapter = new RewardsAdapter(availableRewards, this);
        availableRewardsRecyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        availableRewardsRecyclerView.setAdapter(availableRewardsAdapter);

        // Setup RecyclerView for redeemed rewards
        redeemedRewardsAdapter = new RedeemedRewardsAdapter(redeemedRewards);
        redeemedRewardsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        redeemedRewardsRecyclerView.setAdapter(redeemedRewardsAdapter);

        // Load user points
        loadUserPoints();

        // Load rewards
        loadAvailableRewards();
        loadRedeemedRewards();

        // Setup earn points button
        earnPointsButton.setOnClickListener(v -> {
            // Navigate to Points fragment directly
            navigateToPointsFragment();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeViewModel();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        loadUserPoints();
        loadAvailableRewards();
        loadRedeemedRewards();
    }

    private void observeViewModel() {
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                // Show error message to user
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserPoints() {
        userRepository.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult() != null && isAdded()) {
                User user = task.getResult();
                pointsValueText.setText(String.valueOf(user.getPoints()));
            } else {
                // Default to 0 if points can't be loaded
                pointsValueText.setText("0");
            }
        });
    }

    private void loadAvailableRewards() {
        rewardRepository.getAllRewards(rewards -> {
            if (isAdded()) {
                availableRewards.clear();
                if (rewards != null && !rewards.isEmpty()) {
                    availableRewards.addAll(rewards);
                }
                availableRewardsAdapter.notifyDataSetChanged();
            }
        });
    }

    private void loadRedeemedRewards() {
        rewardRepository.getRedeemedRewards(rewards -> {
            if (isAdded()) {
                redeemedRewards.clear();
                if (rewards != null && !rewards.isEmpty()) {
                    redeemedRewards.addAll(rewards);
                    redeemedRewardsRecyclerView.setVisibility(View.VISIBLE);
                    noRedeemedRewardsText.setVisibility(View.GONE);
                } else {
                    redeemedRewardsRecyclerView.setVisibility(View.GONE);
                    noRedeemedRewardsText.setVisibility(View.VISIBLE);
                }
                redeemedRewardsAdapter.notifyDataSetChanged();
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
                    showRedeemConfirmationDialog(reward);
                } else {
                    Toast.makeText(requireContext(), "Not enough points to redeem this reward", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showRedeemConfirmationDialog(Reward reward) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Redeem Reward")
                .setMessage("Are you sure you want to redeem " + reward.getTitle() + " for " + reward.getPointsRequired() + " points?")
                .setPositiveButton("Redeem", (dialog, which) -> {
                    redeemReward(reward);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void redeemReward(Reward reward) {
        rewardRepository.redeemReward(reward, (success, message) -> {
            if (isAdded()) {
                if (success) {
                    // Show success message
                    Toast.makeText(requireContext(), "Reward redeemed successfully!", Toast.LENGTH_SHORT).show();
                    
                    // Refresh rewards lists and points
                    loadUserPoints();
                    loadAvailableRewards();
                    loadRedeemedRewards();
                } else {
                    // Show error message
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void navigateToPointsFragment() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new com.example.pointbrewproject.ui.points.PointsFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }
} 