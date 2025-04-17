package com.example.pointbrewproject.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private Button saveButton;
    private UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        
        userRepository = UserRepository.getInstance();
        
        // Initialize views
        nameEditText = view.findViewById(R.id.edit_name);
        emailEditText = view.findViewById(R.id.edit_email);
        phoneEditText = view.findViewById(R.id.edit_phone);
        saveButton = view.findViewById(R.id.button_save);
        
        // Set up back button
        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        
        // Load user data
        loadUserData();
        
        // Set up save button
        saveButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Please enter your name", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // In a real app, you would update the user's profile
            // For now, just show a success message and go back
            Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        
        return view;
    }
    
    private void loadUserData() {
        FirebaseUser currentUser = userRepository.getCurrentFirebaseUser();
        if (currentUser != null) {
            userRepository.getUserData(currentUser.getUid(), task -> {
                if (task.isSuccessful() && task.getResult() != null && isAdded()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Load name
                        String fullName = document.getString("fullName");
                        if (fullName != null && !fullName.isEmpty()) {
                            nameEditText.setText(fullName);
                        }
                        
                        // Load email
                        String email = document.getString("email");
                        if (email != null && !email.isEmpty()) {
                            emailEditText.setText(email);
                        } else {
                            // If no email in document, use the one from Firebase Auth
                            emailEditText.setText(currentUser.getEmail());
                        }
                        
                        // Load phone
                        String phone = document.getString("phone");
                        if (phone != null && !phone.isEmpty()) {
                            phoneEditText.setText(phone);
                        }
                    }
                }
            });
        }
    }
} 