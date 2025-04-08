package com.example.pointbrewproject.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.User;
import com.example.pointbrewproject.data.repository.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;

public class AdminDashboardFragment extends Fragment {
    private TextView adminNameTextView;
    private TextView totalUsersTextView;
    private TextView totalRewardsTextView;
    private TextView totalPointsTextView;
    private MaterialButton createRewardButton;
    private MaterialButton generateQrButton;
    private MaterialButton viewUsersButton;
    private UserRepository userRepository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepository = UserRepository.getInstance();
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
            // Navigate to create reward screen
            // Implementation will go here
        });
        
        generateQrButton.setOnClickListener(v -> {
            // Navigate to QR code generation screen
            // Implementation will go here
        });
        
        viewUsersButton.setOnClickListener(v -> {
            // Navigate to users list screen
            // Implementation will go here
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
        // TODO: Implement loading of admin dashboard data
        // This should fetch data from repositories
        
        // For now, just set placeholder values
        totalUsersTextView.setText("0");
        totalRewardsTextView.setText("0");
        totalPointsTextView.setText("0");
    }
} 