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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class RewardsFragment extends Fragment implements RewardsAdapter.OnRewardClickListener {
    private RewardsViewModel viewModel;
    private TextView pointsBalanceText;
    private TextView nextRewardText;
    private RecyclerView rewardsRecyclerView;
    private RewardsAdapter adapter;
    private UserRepository userRepository;
    private RewardRepository rewardRepository;
    private List<Reward> allRewards = new ArrayList<>();

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

        pointsBalanceText = view.findViewById(R.id.points_balance_text);
        nextRewardText = view.findViewById(R.id.next_reward_text);
        rewardsRecyclerView = view.findViewById(R.id.rewards_recycler_view);
        TextInputEditText searchInput = view.findViewById(R.id.search_input);
        Button filterButton = view.findViewById(R.id.filter_button);

        // Setup RecyclerView
        adapter = new RewardsAdapter(allRewards, this::onRewardRedeemed);
        rewardsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        rewardsRecyclerView.setAdapter(adapter);

        // Load user points
        userRepository.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult();
                pointsBalanceText.setText("Your Points: " + user.getPoints());
                updateNextRewardText(user.getPoints());
            }
        });

        // Load rewards
        viewModel.getAvailableRewards().observe(getViewLifecycleOwner(), rewards -> {
            allRewards = rewards;
            adapter.updateRewards(rewards);
        });

        // Setup search
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            String query = v.getText().toString().trim();
            filterRewards(query);
            return true;
        });

        // Setup filter button
        filterButton.setOnClickListener(v -> {
            // Show filter dialog
            showFilterDialog();
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
            }
        });
    }

    private void updateNextRewardText(int currentPoints) {
        // Find the next reward that user can't afford yet
        int nextRewardPoints = Integer.MAX_VALUE;
        for (Reward reward : allRewards) {
            if (reward.getPointsRequired() > currentPoints && reward.getPointsRequired() < nextRewardPoints) {
                nextRewardPoints = reward.getPointsRequired();
            }
        }

        if (nextRewardPoints != Integer.MAX_VALUE) {
            int pointsNeeded = nextRewardPoints - currentPoints;
            nextRewardText.setText(pointsNeeded + " points until next reward");
        } else {
            nextRewardText.setText("You can afford all rewards!");
        }
    }

    private void filterRewards(String query) {
        List<Reward> filteredRewards = new ArrayList<>();
        for (Reward reward : allRewards) {
            if (reward.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                reward.getDescription().toLowerCase().contains(query.toLowerCase())) {
                filteredRewards.add(reward);
            }
        }
        adapter.updateRewards(filteredRewards);
    }

    private void showFilterDialog() {
        // Implement filter dialog to filter by points range, category, etc.
        // This is a placeholder for the actual implementation
        Toast.makeText(requireContext(), "Filter dialog will be implemented", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(requireContext(), "Redeem confirmation dialog will be implemented", Toast.LENGTH_SHORT).show();
    }

    private void onRewardRedeemed(Reward reward) {
        RewardRepository.getInstance().redeemReward(reward, (success, message) -> {
            if (success) {
                // Show success message
            } else {
                // Show error message
            }
        });
    }
} 