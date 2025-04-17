package com.example.pointbrewproject.ui.rewards;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.RedeemedReward;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RedeemedRewardsAdapter extends RecyclerView.Adapter<RedeemedRewardsAdapter.RedeemedRewardViewHolder> {
    private List<RedeemedReward> redeemedRewards;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public RedeemedRewardsAdapter(List<RedeemedReward> redeemedRewards) {
        this.redeemedRewards = redeemedRewards;
    }

    @NonNull
    @Override
    public RedeemedRewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reward_redeemed, parent, false);
        return new RedeemedRewardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RedeemedRewardViewHolder holder, int position) {
        RedeemedReward reward = redeemedRewards.get(position);
        holder.bind(reward);
    }

    @Override
    public int getItemCount() {
        return redeemedRewards.size();
    }

    public void updateRewards(List<RedeemedReward> rewards) {
        this.redeemedRewards = rewards;
        notifyDataSetChanged();
    }

    static class RedeemedRewardViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView descriptionTextView;
        private final TextView dateTextView;
        private final TextView statusTextView;
        private final ImageView rewardImageView;

        public RedeemedRewardViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.reward_title);
            descriptionTextView = itemView.findViewById(R.id.reward_description);
            dateTextView = itemView.findViewById(R.id.redeemed_date);
            statusTextView = itemView.findViewById(R.id.reward_status);
            rewardImageView = itemView.findViewById(R.id.reward_image);
        }

        public void bind(RedeemedReward reward) {
            titleTextView.setText(reward.getRewardTitle());
            descriptionTextView.setText(reward.getRewardDescription());
            
            // Format the redemption date
            Timestamp timestamp = reward.getRedeemedAt();
            String dateText = "Redeemed on " + formatDate(timestamp != null ? timestamp.toDate() : new Date());
            dateTextView.setText(dateText);
            
            // Set status text based on reward type
            String rewardType = reward.getRewardType();
            if (rewardType != null) {
                switch (rewardType) {
                    case "DISCOUNT":
                        statusTextView.setText("Discount");
                        statusTextView.setBackgroundResource(R.drawable.rounded_discount_bg);
                        break;
                    case "ITEM":
                        statusTextView.setText("Item");
                        statusTextView.setBackgroundResource(R.drawable.rounded_item_bg);
                        break;
                    default:
                        statusTextView.setText("Redeemed");
                        statusTextView.setBackgroundResource(R.drawable.rounded_gray_bg);
                        break;
                }
            } else {
                statusTextView.setText("Redeemed");
            }

            // Load image if available
            if (reward.getRewardImageUrl() != null && !reward.getRewardImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(reward.getRewardImageUrl())
                        .placeholder(R.drawable.ic_reward)
                        .into(rewardImageView);
            } else {
                rewardImageView.setImageResource(R.drawable.ic_reward);
            }
        }
        
        private String formatDate(Date date) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(date);
        }
    }
} 