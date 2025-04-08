package com.example.pointbrewproject.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.User;
import com.example.pointbrewproject.data.repository.UserRepository;

public class HomeFragment extends Fragment {

    private TextView greetingText;
    private TextView usernameText;
    private TextView pointsValueText;
    private Button earnPointsButton;
    private RecyclerView rewardsRecyclerView;
    private RecyclerView activitiesRecyclerView;
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
        greetingText = view.findViewById(R.id.text_greeting);
        usernameText = view.findViewById(R.id.text_username);
        pointsValueText = view.findViewById(R.id.text_points_value);
        earnPointsButton = view.findViewById(R.id.button_earn_points);
        rewardsRecyclerView = view.findViewById(R.id.recycler_rewards);
        activitiesRecyclerView = view.findViewById(R.id.recycler_activities);
        
        // Initialize section headers
        TextView rewardsTitle = view.findViewById(R.id.section_rewards).findViewById(R.id.section_title);
        rewardsTitle.setText("Available Rewards");
        
        TextView activitiesTitle = view.findViewById(R.id.section_recent_activities).findViewById(R.id.section_title);
        activitiesTitle.setText("Recent Activities");
        
        // Initialize repository
        userRepository = UserRepository.getInstance();
        
        // Load user data
        loadUserData();
        
        // Set up earn points button
        earnPointsButton.setOnClickListener(v -> {
            // Navigate to earn points fragment or show QR scanner
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
            }
        });
    }
} 