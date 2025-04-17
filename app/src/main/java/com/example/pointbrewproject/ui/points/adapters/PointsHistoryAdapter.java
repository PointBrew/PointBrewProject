package com.example.pointbrewproject.ui.points.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.PointsActivity;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PointsHistoryAdapter extends RecyclerView.Adapter<PointsHistoryAdapter.PointsHistoryViewHolder> {
    private List<PointsActivity> pointsActivities;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public PointsHistoryAdapter(List<PointsActivity> pointsActivities) {
        this.pointsActivities = pointsActivities;
    }

    @NonNull
    @Override
    public PointsHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_points_history, parent, false);
        return new PointsHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PointsHistoryViewHolder holder, int position) {
        PointsActivity activity = pointsActivities.get(position);
        holder.bind(activity);
    }

    @Override
    public int getItemCount() {
        return pointsActivities.size();
    }

    public void updateActivities(List<PointsActivity> activities) {
        this.pointsActivities = activities;
        notifyDataSetChanged();
    }

    static class PointsHistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView descriptionTextView;
        private final TextView pointsTextView;
        private final TextView dateTextView;
        private final ImageView iconImageView;

        public PointsHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.text_activity_description);
            pointsTextView = itemView.findViewById(R.id.text_points_value);
            dateTextView = itemView.findViewById(R.id.text_activity_date);
            iconImageView = itemView.findViewById(R.id.image_activity_icon);
        }

        public void bind(PointsActivity activity) {
            descriptionTextView.setText(activity.getDescription());
            
            // Format points with + or - sign
            int points = activity.getPoints();
            String pointsText = activity.isEarned() ? 
                    "+" + points : 
                    String.valueOf(points); // Points spent are already negative in the model
            
            pointsTextView.setText(pointsText);
            
            // Set color based on earned or spent
            int textColor = activity.isEarned() ? 
                    ContextCompat.getColor(itemView.getContext(), R.color.primary) : 
                    ContextCompat.getColor(itemView.getContext(), R.color.accent);
            pointsTextView.setTextColor(textColor);
            
            // Format date
            Timestamp timestamp = activity.getTimestamp();
            if (timestamp != null) {
                dateTextView.setText(formatDate(timestamp.toDate()));
            } else {
                dateTextView.setText(formatDate(new Date()));
            }
            
            // Set icon based on source type
            String sourceType = activity.getSourceType();
            if (sourceType != null) {
                switch (sourceType) {
                    case "QR_CODE":
                        iconImageView.setImageResource(R.drawable.ic_qr_code);
                        break;
                    case "REWARD_REDEMPTION":
                        iconImageView.setImageResource(R.drawable.ic_reward);
                        break;
                    default:
                        iconImageView.setImageResource(R.drawable.ic_points);
                        break;
                }
            } else {
                iconImageView.setImageResource(R.drawable.ic_points);
            }
        }
        
        private String formatDate(Date date) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(date);
        }
    }
} 