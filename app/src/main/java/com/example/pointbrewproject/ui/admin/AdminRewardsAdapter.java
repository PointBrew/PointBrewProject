package com.example.pointbrewproject.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.Reward;
import com.google.android.material.chip.Chip;
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
        private final Chip pointsChip;
        private final TextView expirationText;
        private final ImageButton deleteButton;
        private final ImageView rewardImage;

        public RewardViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_reward_title);
            descriptionText = itemView.findViewById(R.id.text_reward_description);
            pointsChip = itemView.findViewById(R.id.chip_reward_points);
            expirationText = itemView.findViewById(R.id.text_reward_expiration);
            deleteButton = itemView.findViewById(R.id.button_delete_reward);
            rewardImage = itemView.findViewById(R.id.image_reward);
        }

        public void bind(Reward reward, OnRewardClickListener listener) {
            titleText.setText(reward.getTitle());
            descriptionText.setText(reward.getDescription());
            pointsChip.setText(String.format("%d points", reward.getPointsRequired()));
            
            // Set expiration date
            String expirationText = reward.getExpirationDate() != null && !reward.getExpirationDate().isEmpty() 
                    ? "Expires: " + reward.getExpirationDate()
                    : "No expiration";
            this.expirationText.setText(expirationText);

            // Load the reward image if available
            if (reward.getImageUrl() != null && !reward.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(reward.getImageUrl())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .centerCrop()
                        .error(R.drawable.ic_reward)
                        .into(rewardImage);
            } else {
                // Set a default reward image
                rewardImage.setImageResource(R.drawable.ic_reward);
                rewardImage.setScaleType(ImageView.ScaleType.CENTER);
            }

            // Set click listeners
            itemView.setOnClickListener(v -> listener.onRewardClick(reward));
            deleteButton.setOnClickListener(v -> listener.onDeleteClick(reward));
        }
    }
} 