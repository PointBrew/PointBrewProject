package com.example.pointbrewproject.ui.admin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.Reward;
import com.example.pointbrewproject.data.repository.RewardRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;

public class AdminRewardsFragment extends Fragment implements AdminRewardsAdapter.OnRewardClickListener {
    private RecyclerView rewardsRecyclerView;
    private AdminRewardsAdapter adapter;
    private List<Reward> rewards = new ArrayList<>();
    private RewardRepository rewardRepository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rewardRepository = RewardRepository.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_rewards, container, false);

        rewardsRecyclerView = view.findViewById(R.id.rewards_recycler_view);
        rewardsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminRewardsAdapter(rewards, this);
        rewardsRecyclerView.setAdapter(adapter);

        FloatingActionButton fabAddReward = view.findViewById(R.id.fab_add_reward);
        fabAddReward.setOnClickListener(v -> showRewardDialog(null));

        loadRewards();

        return view;
    }

    private void loadRewards() {
        rewardRepository.getAllRewards(new RewardRepository.RewardCallback() {
            @Override
            public void onComplete(List<Reward> rewardList) {
                rewards.clear();
                rewards.addAll(rewardList);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showRewardDialog(@Nullable Reward reward) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_reward_form, null);
        builder.setView(dialogView);

        TextInputEditText editTitle = dialogView.findViewById(R.id.edit_title);
        TextInputEditText editDescription = dialogView.findViewById(R.id.edit_description);
        TextInputEditText editPoints = dialogView.findViewById(R.id.edit_points);
        TextInputEditText editExpiration = dialogView.findViewById(R.id.edit_expiration);

        if (reward != null) {
            editTitle.setText(reward.getTitle());
            editDescription.setText(reward.getDescription());
            editPoints.setText(String.valueOf(reward.getPointsRequired()));
            editExpiration.setText(reward.getExpirationDate());
        }

        Dialog dialog = builder.create();
        dialogView.findViewById(R.id.button_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.button_save).setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String description = editDescription.getText().toString().trim();
            String pointsStr = editPoints.getText().toString().trim();
            String expiration = editExpiration.getText().toString().trim();

            if (title.isEmpty() || description.isEmpty() || pointsStr.isEmpty() || expiration.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int points = Integer.parseInt(pointsStr);
            if (reward == null) {
                // Create new reward
                Reward newReward = new Reward();
                newReward.setTitle(title);
                newReward.setDescription(description);
                newReward.setPointsRequired(points);
                newReward.setExpirationDate(expiration);
                rewardRepository.addReward(newReward, new RewardRepository.RewardCallback() {
                    @Override
                    public void onComplete(List<Reward> rewardList) {
                        loadRewards();
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Reward added successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // Update existing reward
                reward.setTitle(title);
                reward.setDescription(description);
                reward.setPointsRequired(points);
                reward.setExpirationDate(expiration);
                rewardRepository.updateReward(reward, new RewardRepository.RewardCallback() {
                    @Override
                    public void onComplete(List<Reward> rewardList) {
                        loadRewards();
                        dialog.dismiss();
                        Toast.makeText(getContext(), "Reward updated successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.show();
    }

    @Override
    public void onRewardClick(Reward reward) {
        showRewardDialog(reward);
    }

    @Override
    public void onDeleteClick(Reward reward) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Reward")
                .setMessage("Are you sure you want to delete this reward?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    rewardRepository.deleteReward(reward.getId(), new RewardRepository.RewardCallback() {
                        @Override
                        public void onComplete(List<Reward> rewardList) {
                            loadRewards();
                            Toast.makeText(getContext(), "Reward deleted successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
} 