package com.example.pointbrewproject.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.User;
import com.example.pointbrewproject.data.repository.UserRepository;
import com.google.android.material.button.MaterialButton;
import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment {

    private TextView greetingText;
    private TextView usernameText;
    private TextView pointsValueText;
    private MaterialButton viewRewardsButton;
    private RecyclerView featuredDrinksRecyclerView;
    private RecyclerView recentOrdersRecyclerView;
    private CircleImageView userImageView;
    private UserRepository userRepository;

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
        
        // Initialize views
        greetingText = view.findViewById(R.id.greeting_text);
        usernameText = view.findViewById(R.id.user_name);
        userImageView = view.findViewById(R.id.user_image);
        pointsValueText = view.findViewById(R.id.points_value);
        viewRewardsButton = view.findViewById(R.id.view_rewards_button);
        featuredDrinksRecyclerView = view.findViewById(R.id.featured_drinks_recycler);
        recentOrdersRecyclerView = view.findViewById(R.id.recent_orders_recycler);
        
        // Initialize repository
        userRepository = UserRepository.getInstance();
        
        // Load user data
        loadUserData();
        
        // Set up view rewards button
        viewRewardsButton.setOnClickListener(v -> {
            // Navigate to rewards fragment
            // Implementation will go here
        });
        
        // Initialize adapters for recycler views
        // This will be implemented later
    }
    
    private void loadUserData() {
        userRepository.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult();
                usernameText.setText(user.getName());
                pointsValueText.setText(String.valueOf(user.getPoints()));
                
                // Load profile image if available
                if (user.getProfilePictureUrl() != null && getContext() != null) {
                    // Use Glide or similar library to load image
                    // Implementation will go here
                }
            }
        });
    }
} 