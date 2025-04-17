package com.example.pointbrewproject.ui.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.Reward;
import com.example.pointbrewproject.data.repository.RewardRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;
import android.app.DatePickerDialog;

public class AdminRewardsFragment extends Fragment implements AdminRewardsAdapter.OnRewardClickListener {
    private RecyclerView rewardsRecyclerView;
    private AdminRewardsAdapter adapter;
    private List<Reward> rewards = new ArrayList<>();
    private RewardRepository rewardRepository;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri = null;
    private ImageView rewardImageView;

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            
            // Load the selected image into the ImageView
            if (rewardImageView != null && selectedImageUri != null) {
                Glide.with(this)
                    .load(selectedImageUri)
                    .centerCrop()
                    .into(rewardImageView);
            }
        }
    }

    private void showRewardDialog(Reward reward) {
        // Create the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_reward, null);
        builder.setView(dialogView);
        
        // Initialize dialog views
        TextInputEditText titleEditText = dialogView.findViewById(R.id.edit_reward_title);
        TextInputEditText descriptionEditText = dialogView.findViewById(R.id.edit_reward_description);
        TextInputEditText pointsEditText = dialogView.findViewById(R.id.edit_reward_points);
        TextInputEditText expirationEditText = dialogView.findViewById(R.id.edit_reward_expiration);
        rewardImageView = dialogView.findViewById(R.id.image_reward);
        Button selectImageButton = dialogView.findViewById(R.id.button_select_image);
        
        // Make the expiration field read-only and add click listener for date picker
        expirationEditText.setFocusable(false);
        expirationEditText.setClickable(true);
        expirationEditText.setOnClickListener(v -> showDatePickerDialog(expirationEditText));
        
        // Pre-fill fields if editing existing reward
        if (reward != null) {
            titleEditText.setText(reward.getTitle());
            descriptionEditText.setText(reward.getDescription());
            pointsEditText.setText(String.valueOf(reward.getPointsRequired()));
            expirationEditText.setText(reward.getExpirationDate());
            // Load image if URL exists
            if (reward.getImageUrl() != null && !reward.getImageUrl().isEmpty()) {
                Glide.with(this)
                    .load(reward.getImageUrl())
                    .centerCrop()
                    .into(rewardImageView);
            }
        }
        
        // Image selection
        selectImageButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Reward Image"), PICK_IMAGE_REQUEST);
        });

        // Create dialog and set action buttons
        Dialog dialog = builder.create();
        dialog.show();
        
        // Save button click listener
        dialogView.findViewById(R.id.button_save).setOnClickListener(v -> {
            String title = titleEditText.getText().toString().trim();
            String description = descriptionEditText.getText().toString().trim();
            String pointsStr = pointsEditText.getText().toString().trim();
            String expiration = expirationEditText.getText().toString().trim();
            
            if (title.isEmpty() || description.isEmpty() || pointsStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all required fields", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int points;
            try {
                points = Integer.parseInt(pointsStr);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Please enter a valid number for points", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Create or update reward
            String imageUrl = selectedImageUri != null ? selectedImageUri.toString() : 
                             (reward != null ? reward.getImageUrl() : "");
            
            if (reward == null) {
                // Create new reward
                Reward newReward = new Reward();
                newReward.setTitle(title);
                newReward.setDescription(description);
                newReward.setPointsRequired(points);
                newReward.setExpirationDate(expiration);
                newReward.setImageUrl(imageUrl);
                
                saveReward(newReward);
            } else {
                // Update existing reward
                reward.setTitle(title);
                reward.setDescription(description);
                reward.setPointsRequired(points);
                reward.setExpirationDate(expiration);
                reward.setImageUrl(imageUrl);
                
                updateReward(reward);
            }
            
            dialog.dismiss();
        });
        
        // Cancel button click listener
        dialogView.findViewById(R.id.button_cancel).setOnClickListener(v -> dialog.dismiss());
    }
    
    private void saveReward(Reward reward) {
        rewardRepository.addReward(reward, rewards -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Reward created successfully", Toast.LENGTH_SHORT).show();
                    loadRewards();
                });
            }
        });
    }
    
    private void updateReward(Reward reward) {
        rewardRepository.updateReward(reward, rewards -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "Reward updated successfully", Toast.LENGTH_SHORT).show();
                    loadRewards();
                });
            }
        });
    }

    private void loadRewards() {
        rewardRepository.getAllRewards(rewardList -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    rewards.clear();
                    if (rewardList != null) {
                        rewards.addAll(rewardList);
                    }
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }

    /**
     * Shows a date picker dialog and sets the selected date in the provided EditText
     */
    private void showDatePickerDialog(TextInputEditText dateField) {
        // Use the current date as the default date in the picker
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format the date
                    calendar.set(selectedYear, selectedMonth, selectedDay);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
                    String formattedDate = dateFormat.format(calendar.getTime());
                    
                    // Set the formatted date in the EditText
                    dateField.setText(formattedDate);
                },
                year, month, day);
        
        // Set the minimum date to today
        datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
        
        // Show the dialog
        datePickerDialog.show();
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
                rewardRepository.deleteReward(reward.getId(), rewards -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Reward deleted successfully", Toast.LENGTH_SHORT).show();
                            loadRewards();
                        });
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
} 