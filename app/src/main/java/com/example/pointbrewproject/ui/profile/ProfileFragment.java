package com.example.pointbrewproject.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.User;
import com.example.pointbrewproject.data.repository.UserRepository;
import com.example.pointbrewproject.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

public class ProfileFragment extends Fragment {

    private TextView nameTextView;
    private TextView emailTextView;
    private ImageView profileImageView;
    private Button logoutButton;
    private UserRepository userRepository;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        userRepository = UserRepository.getInstance();
        
        // Initialize views
        nameTextView = view.findViewById(R.id.profile_name);
        emailTextView = view.findViewById(R.id.profile_email);
        profileImageView = view.findViewById(R.id.profile_image);
        logoutButton = view.findViewById(R.id.logout_button);
        
        // Set up logout button
        logoutButton.setOnClickListener(v -> {
            userRepository.logout();
            // Navigate to login activity
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
        
        // Load user data
        loadUserData();
    }
    
    private void loadUserData() {
        FirebaseUser currentUser = userRepository.getCurrentFirebaseUser();
        if (currentUser != null) {
            // Get user data from Firestore
            userRepository.getUserData(currentUser.getUid(), task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Set name
                        String fullName = document.getString("fullName");
                        if (fullName != null && !fullName.isEmpty()) {
                            nameTextView.setText(fullName);
                        } else {
                            nameTextView.setText("User");
                        }
                        
                        // Set email
                        String email = document.getString("email");
                        if (email != null) {
                            emailTextView.setText(email);
                        } else {
                            emailTextView.setText(currentUser.getEmail());
                        }
                        
                        // Profile image would be loaded using a library like Glide in a real app
                        // For now, we'll use a placeholder
                        profileImageView.setImageResource(R.drawable.ic_profile);
                    }
                }
            });
        }
    }
} 