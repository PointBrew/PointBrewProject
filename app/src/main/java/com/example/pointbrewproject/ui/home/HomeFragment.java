package com.example.pointbrewproject.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.Reward;
import com.example.pointbrewproject.data.model.User;
import com.example.pointbrewproject.data.repository.RewardRepository;
import com.example.pointbrewproject.data.repository.UserRepository;
import com.example.pointbrewproject.ui.points.PointsFragment;
import com.example.pointbrewproject.ui.rewards.RewardsFragment;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TextView pointsValueText;
    private TextView viewMoreRewards;
    private MaterialButton viewRewardsButton;
    private MaterialButton scanQrCodeButton;
    private RecyclerView featuredRewardsRecyclerView;
    private UserRepository userRepository;
    private RewardRepository rewardRepository;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize repositories
        userRepository = UserRepository.getInstance();
        rewardRepository = RewardRepository.getInstance();
        
        // Initialize views
        pointsValueText = view.findViewById(R.id.points_value);
        viewRewardsButton = view.findViewById(R.id.view_rewards_button);
        scanQrCodeButton = view.findViewById(R.id.scan_qr_code_button);
        featuredRewardsRecyclerView = view.findViewById(R.id.featured_rewards_recycler);
        viewMoreRewards = view.findViewById(R.id.view_more_rewards);
        
        // Load user data
        loadUserData();
        
        // Load featured rewards
        loadFeaturedRewards();
        
        // Set up view rewards button - navigate to rewards fragment
        viewRewardsButton.setOnClickListener(v -> {
            navigateToFragment(new RewardsFragment());
        });
        
        // Set up view more rewards button
        viewMoreRewards.setOnClickListener(v -> {
            navigateToFragment(new RewardsFragment());
        });
        
        // Set up scan QR code button - navigate to points fragment instead of scanner
        scanQrCodeButton.setOnClickListener(v -> {
            navigateToFragment(new PointsFragment());
        });
    }
    
    private void navigateToFragment(Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
    
    private void loadUserData() {
        userRepository.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult() != null && isAdded()) {
                User user = task.getResult();
                pointsValueText.setText(String.valueOf(user.getPoints()));
            }
        });
    }
    
    private void loadFeaturedRewards() {
        rewardRepository.getAllRewards(rewards -> {
            if (isAdded()) {
                // Display only up to 3 rewards
                List<Reward> featuredRewards = new ArrayList<>();
                int count = Math.min(rewards.size(), 3);
                for (int i = 0; i < count; i++) {
                    featuredRewards.add(rewards.get(i));
                }
                
                // Set up grid layout for rewards (2 columns)
                GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
                featuredRewardsRecyclerView.setLayoutManager(layoutManager);
                
                // Create and set adapter for rewards
                RewardAdapter adapter = new RewardAdapter(featuredRewards);
                featuredRewardsRecyclerView.setAdapter(adapter);
                
                // Show/hide view more based on if there are more than 3 rewards
                viewMoreRewards.setVisibility(rewards.size() > 3 ? View.VISIBLE : View.GONE);
            }
        });
    }
    
    // Reward adapter class for the featured rewards
    private class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.RewardViewHolder> {
        
        private List<Reward> rewards;
        
        RewardAdapter(List<Reward> rewards) {
            this.rewards = rewards;
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
            holder.titleText.setText(reward.getTitle());
            holder.descriptionText.setText(reward.getDescription());
            holder.pointsText.setText(reward.getPointsRequired() + " points");
            
            // Set click listener to navigate to rewards page
            holder.itemView.setOnClickListener(v -> {
                navigateToFragment(new RewardsFragment());
            });
        }
        
        @Override
        public int getItemCount() {
            return rewards.size();
        }
        
        class RewardViewHolder extends RecyclerView.ViewHolder {
            TextView titleText;
            TextView descriptionText;
            TextView pointsText;
            ImageView rewardImage;
            
            RewardViewHolder(@NonNull View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.text_reward_title);
                descriptionText = itemView.findViewById(R.id.text_reward_description);
                pointsText = itemView.findViewById(R.id.chip_reward_points);
                rewardImage = itemView.findViewById(R.id.image_reward);
            }
        }
    }
} 