package com.example.pointbrewproject.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.User;
import com.example.pointbrewproject.data.repository.QRCodeRepository;
import com.example.pointbrewproject.data.repository.RewardRepository;
import com.example.pointbrewproject.data.repository.UserRepository;

import java.util.concurrent.atomic.AtomicInteger;

public class AdminDashboardFragment extends Fragment {
    private TextView adminNameTextView;
    private TextView totalUsersTextView;
    private TextView totalRewardsTextView;
    private TextView totalPointsTextView;
    private MaterialButton createRewardButton;
    private MaterialButton generateQrButton;
    private MaterialButton viewUsersButton;
    private UserRepository userRepository;
    private RewardRepository rewardRepository;
    private QRCodeRepository qrCodeRepository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepository = UserRepository.getInstance();
        rewardRepository = RewardRepository.getInstance();
        qrCodeRepository = QRCodeRepository.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);
        // Initialize views
        adminNameTextView = view.findViewById(R.id.text_admin_name);
        totalUsersTextView = view.findViewById(R.id.text_total_users);
        totalRewardsTextView = view.findViewById(R.id.text_total_rewards);
        totalPointsTextView = view.findViewById(R.id.text_total_points);
        createRewardButton = view.findViewById(R.id.button_create_reward);
        generateQrButton = view.findViewById(R.id.button_generate_qr);
        viewUsersButton = view.findViewById(R.id.button_view_users);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadAdminData();
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        createRewardButton.setOnClickListener(v -> {
            // Navigate to manage rewards screen using fragment transaction
            if (getActivity() != null) {
                // Update bottom navigation selection
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                bottomNav.setSelectedItemId(R.id.navigation_admin_rewards);
                
                // The fragment will be loaded by the bottom navigation listener
            }
        });
        
        generateQrButton.setOnClickListener(v -> {
            // Navigate to QR code generation screen using fragment transaction
            if (getActivity() != null) {
                // Update bottom navigation selection
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                bottomNav.setSelectedItemId(R.id.navigation_qr_code);
                
                // The fragment will be loaded by the bottom navigation listener
            }
        });
        
        viewUsersButton.setOnClickListener(v -> {
            // Navigate to users list screen using fragment transaction
            if (getActivity() != null) {
                // Update bottom navigation selection
                BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
                bottomNav.setSelectedItemId(R.id.navigation_manage_users);
                
                // The fragment will be loaded by the bottom navigation listener
            }
        });
    }

    private void loadAdminData() {
        userRepository.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User adminUser = task.getResult();
                if (adminUser != null && adminUser.isAdmin()) {
                    // Set admin name
                    adminNameTextView.setText(adminUser.getName());
                    // Load admin dashboard data
                    loadDashboardData();
                }
            }
        });
    }

    private void loadDashboardData() {
        // Load total users count
        userRepository.getAllUsers(usersTask -> {
            if (usersTask.isSuccessful() && usersTask.getResult() != null) {
                int userCount = usersTask.getResult().size();
                totalUsersTextView.setText(String.valueOf(userCount));
            } else {
                totalUsersTextView.setText("0");
            }
        });
        
        // Load total rewards count
        rewardRepository.getAllRewards(rewards -> {
            int rewardsCount = rewards != null ? rewards.size() : 0;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> 
                    totalRewardsTextView.setText(String.valueOf(rewardsCount))
                );
            }
        });
        
        // Calculate total points distributed
        calculateTotalPointsDistributed();
    }
    
    private void calculateTotalPointsDistributed() {
        // Get all users and sum their points
        userRepository.getAllUsers(usersTask -> {
            if (usersTask.isSuccessful() && usersTask.getResult() != null) {
                AtomicInteger totalPoints = new AtomicInteger(0);
                
                for (User user : usersTask.getResult()) {
                    totalPoints.addAndGet(user.getPoints());
                }
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                        totalPointsTextView.setText(String.valueOf(totalPoints.get()))
                    );
                }
            } else {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> totalPointsTextView.setText("0"));
                }
            }
        });
    }
} 