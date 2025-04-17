package com.example.pointbrewproject.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

public class PersonalInfoFragment extends Fragment {

    private TextView titleTextView;
    private UserRepository userRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_info, container, false);
        titleTextView = view.findViewById(R.id.title_text);
        
        userRepository = UserRepository.getInstance();
        
        // Set up back button
        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        
        loadUserData();
        
        return view;
    }
    
    private void loadUserData() {
        FirebaseUser currentUser = userRepository.getCurrentFirebaseUser();
        if (currentUser != null) {
            userRepository.getUserData(currentUser.getUid(), task -> {
                if (task.isSuccessful() && task.getResult() != null && isAdded()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // Here you would load and display user personal info
                    }
                }
            });
        }
    }
} 