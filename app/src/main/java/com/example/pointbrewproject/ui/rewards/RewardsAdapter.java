package com.example.pointbrewproject.ui.rewards;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.Reward;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class RewardsAdapter extends RecyclerView.Adapter<RewardsAdapter.RewardViewHolder> {
    private List<Reward> rewards;
    private final OnRewardClickListener listener;

    public interface OnRewardClickListener {
        void onRewardClick(Reward reward);
    }

    public RewardsAdapter(List<Reward> rewards, OnRewardClickListener listener) {
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
        this.rewards = newRewards;
        notifyDataSetChanged();
    }

    static class RewardViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final TextView pointsTextView;
        private final ImageView rewardImageView;
        private MaterialButton redeemButton;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.text_reward_title);
            descriptionTextView = itemView.findViewById(R.id.text_reward_description);
            pointsTextView = itemView.findViewById(R.id.chip_reward_points);
            rewardImageView = itemView.findViewById(R.id.image_reward);
            
            // This might be null in admin layout, so handle it safely
            redeemButton = itemView.findViewById(R.id.redeem_button);
        }

        public void bind(Reward reward, OnRewardClickListener listener) {
            titleTextView.setText(reward.getTitle());
            descriptionTextView.setText(reward.getDescription());
            pointsTextView.setText(reward.getPointsRequired() + " points");

            // Load image if available
            if (reward.getImageUrl() != null && !reward.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(reward.getImageUrl())
                        .placeholder(R.drawable.ic_reward)
                        .into(rewardImageView);
            } else {
                rewardImageView.setImageResource(R.drawable.ic_reward);
            }

            // Handle click on the whole item if redeem button is not available
            if (redeemButton != null) {
                redeemButton.setOnClickListener(v -> listener.onRewardClick(reward));
            } else {
                itemView.setOnClickListener(v -> listener.onRewardClick(reward));
            }
        }
    }
} 