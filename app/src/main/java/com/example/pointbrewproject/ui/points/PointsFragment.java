package com.example.pointbrewproject.ui.points;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.PointsActivity;
import com.example.pointbrewproject.data.model.User;
import com.example.pointbrewproject.data.repository.QRCodeRepository;
import com.example.pointbrewproject.data.repository.RewardRepository;
import com.example.pointbrewproject.data.repository.UserRepository;
import com.example.pointbrewproject.ui.points.adapters.PointsHistoryAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class PointsFragment extends Fragment {
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int QR_SCAN_REQUEST = 101;

    private TextView pointsBalanceText;
    private TextView resultMessage;
    private TextInputEditText manualCodeInput;
    private RecyclerView pointsHistoryRecycler;
    private TextView emptyHistoryText;
    
    private UserRepository userRepository;
    private QRCodeRepository qrCodeRepository;
    private RewardRepository rewardRepository;
    private PointsHistoryAdapter pointsHistoryAdapter;
    private List<PointsActivity> pointsActivities = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepository = UserRepository.getInstance();
        qrCodeRepository = QRCodeRepository.getInstance();
        rewardRepository = RewardRepository.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_points, container, false);

        // Initialize views
        pointsBalanceText = view.findViewById(R.id.points_balance_text);
        resultMessage = view.findViewById(R.id.result_message);
        manualCodeInput = view.findViewById(R.id.manual_code_input);
        pointsHistoryRecycler = view.findViewById(R.id.points_history_recycler);
        emptyHistoryText = view.findViewById(R.id.empty_history_text);
        MaterialButton scanQrButton = view.findViewById(R.id.scan_qr_button);
        MaterialButton submitCodeButton = view.findViewById(R.id.submit_code_button);

        // Set up recycler view
        pointsHistoryAdapter = new PointsHistoryAdapter(pointsActivities);
        pointsHistoryRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        pointsHistoryRecycler.setAdapter(pointsHistoryAdapter);
        
        // Load user points and history
        loadUserPoints();
        loadPointsHistory();
        
        // Set up scan QR button
        scanQrButton.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.CAMERA},
                        CAMERA_PERMISSION_REQUEST);
            } else {
                launchScanner();
            }
        });
        
        // Set up manual code submit button
        submitCodeButton.setOnClickListener(v -> {
            String code = manualCodeInput.getText().toString().trim();
            if (code.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a code", Toast.LENGTH_SHORT).show();
                return;
            }
            
            processCode(code);
            manualCodeInput.setText("");
        });

        return view;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to fragment
        loadUserPoints();
        loadPointsHistory();
    }

    private void loadUserPoints() {
        userRepository.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult() != null && isAdded()) {
                User user = task.getResult();
                String pointsText = user.getPoints() > 0 
                    ? String.valueOf(user.getPoints()) 
                    : "0"; // Default to 0 if no points
                pointsBalanceText.setText("Your Points: " + pointsText);
            } else if (isAdded()) {
                // Default to 0 if unable to load
                pointsBalanceText.setText("Your Points: 0");
            }
        });
    }
    
    private void loadPointsHistory() {
        rewardRepository.getPointsActivities(activities -> {
            if (isAdded()) {
                pointsActivities.clear();
                
                if (activities != null && !activities.isEmpty()) {
                    pointsActivities.addAll(activities);
                    pointsHistoryRecycler.setVisibility(View.VISIBLE);
                    emptyHistoryText.setVisibility(View.GONE);
                } else {
                    pointsHistoryRecycler.setVisibility(View.GONE);
                    emptyHistoryText.setVisibility(View.VISIBLE);
                }
                
                pointsHistoryAdapter.notifyDataSetChanged();
            }
        });
    }
    
    private void launchScanner() {
        // Launch the QR scanner activity
        Intent intent = new Intent(getActivity(), ScannerActivity.class);
        startActivityForResult(intent, QR_SCAN_REQUEST);
    }
    
    private void processCode(String code) {
        // Hide result message while processing
        resultMessage.setVisibility(View.GONE);
        
        // Process the code
        qrCodeRepository.processQRCode(code, new QRCodeRepository.QRCodeOperationCallback() {
            @Override
            public void onComplete(boolean success, String message) {
                if (isAdded()) {
                    if (success) {
                        showResult("Success! " + message);
                    } else {
                        showResult("Error: " + message);
                    }
                    
                    // Refresh points display and history
                    loadUserPoints();
                    loadPointsHistory();
                }
            }
        });
    }
    
    private void showResult(String message) {
        resultMessage.setText(message);
        resultMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == QR_SCAN_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            String scannedCode = data.getStringExtra("SCANNED_CODE");
            if (scannedCode != null && !scannedCode.isEmpty()) {
                processCode(scannedCode);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchScanner();
            } else {
                Toast.makeText(requireContext(), "Camera permission is required for scanning", Toast.LENGTH_SHORT).show();
            }
        }
    }
} 