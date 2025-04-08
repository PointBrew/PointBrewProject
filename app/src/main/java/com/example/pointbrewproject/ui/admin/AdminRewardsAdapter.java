package com.example.pointbrewproject.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.Reward;
import java.util.List;

public class AdminRewardsAdapter extends RecyclerView.Adapter<AdminRewardsAdapter.RewardViewHolder> {
    private final List<Reward> rewards;
    private final OnRewardClickListener listener;

    public interface OnRewardClickListener {
        void onRewardClick(Reward reward);
        void onDeleteClick(Reward reward);
    }

    public AdminRewardsAdapter(List<Reward> rewards, OnRewardClickListener listener) {
        this.rewards = rewards;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reward_admin, parent, false);
        return new RewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
        Reward reward = rewards.get(position);
        holder.bind(reward, listener);
    }

    @Override
    public int getItemCount() {
        return rewards.size();
    }

    public void updateRewards(List<Reward> newRewards) {
        rewards.clear();
        rewards.addAll(newRewards);
        notifyDataSetChanged();
    }

    static class RewardViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView descriptionText;
        private final TextView pointsText;
        private final TextView expirationText;
        private final ImageButton deleteButton;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_reward_title);
            descriptionText = itemView.findViewById(R.id.text_reward_description);
            pointsText = itemView.findViewById(R.id.text_reward_points);
            expirationText = itemView.findViewById(R.id.text_reward_expiration);
            deleteButton = itemView.findViewById(R.id.button_delete_reward);
        }

        public void bind(Reward reward, OnRewardClickListener listener) {
            titleText.setText(reward.getTitle());
            descriptionText.setText(reward.getDescription());
            pointsText.setText(String.format("%d points", reward.getPointsRequired()));
            expirationText.setText(String.format("Expires: %s", reward.getExpirationDate()));

            itemView.setOnClickListener(v -> listener.onRewardClick(reward));
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(reward));
        }
    }
} 