package com.example.pointbrewproject.ui.points;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.QRCode;
import com.example.pointbrewproject.data.model.User;
import com.example.pointbrewproject.data.repository.QRCodeRepository;
import com.example.pointbrewproject.data.repository.UserRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.Result;

public class EarnPointsFragment extends Fragment {
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    
    private TextView pointsBalanceText;
    private TextView resultText;
    private CardView resultCard;
    private CardView scanCard;
    private CardView manualInputCard;
    private TextView separatorText;
    private FrameLayout scannerContainer;
    private MaterialButton scanButton;
    private MaterialButton closeButton;
    private MaterialButton submitButton;
    private TextInputEditText codeInput;
    
    private CodeScanner codeScanner;
    private UserRepository userRepository;
    private QRCodeRepository qrCodeRepository;
    
    private boolean isScannerActive = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepository = UserRepository.getInstance();
        qrCodeRepository = QRCodeRepository.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_earn_points, container, false);

        // Initialize views
        pointsBalanceText = view.findViewById(R.id.points_balance_text);
        resultText = view.findViewById(R.id.result_text);
        resultCard = view.findViewById(R.id.result_card);
        scanCard = view.findViewById(R.id.scan_card);
        manualInputCard = view.findViewById(R.id.manual_input_card);
        separatorText = view.findViewById(R.id.separator_text);
        scannerContainer = view.findViewById(R.id.scanner_container);
        scanButton = view.findViewById(R.id.scan_button);
        submitButton = view.findViewById(R.id.submit_button);
        codeInput = view.findViewById(R.id.code_input);

        // Set up the close button dynamically - will be added to scanner
        closeButton = new MaterialButton(requireContext());
        closeButton.setText("Cancel");
        closeButton.setOnClickListener(v -> stopScanner());

        // Load current points balance
        loadUserPoints();

        // Set up the scan button
        scanButton.setOnClickListener(v -> {
            if (isScannerActive) {
                // Stop scanning
                stopScanner();
            } else {
                // Start scanning
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(requireActivity(),
                            new String[]{Manifest.permission.CAMERA},
                            CAMERA_PERMISSION_REQUEST);
                } else {
                    startScanner();
                }
            }
        });

        // Set up the submit button for manual code entry
        submitButton.setOnClickListener(v -> {
            String code = codeInput.getText().toString().trim();
            if (!code.isEmpty()) {
                processManualCode(code);
            } else {
                Toast.makeText(requireContext(), "Please enter a code", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadUserPoints() {
        userRepository.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult() != null && isAdded()) {
                User user = task.getResult();
                pointsBalanceText.setText("Your Points: " + user.getPoints());
            }
        });
    }

    private void startScanner() {
        if (getContext() == null) return;
        
        // Create scanner view
        CodeScannerView scannerView = new CodeScannerView(requireContext());
        scannerContainer.removeAllViews();
        scannerContainer.addView(scannerView);

        // Add a close button in the scanner view
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = 50;
        closeButton.setLayoutParams(params);
        scannerContainer.addView(closeButton);
        
        // Show scanner and hide manual input
        scannerContainer.setVisibility(View.VISIBLE);
        scanButton.setText("Stop Scanning");
        separatorText.setVisibility(View.GONE);
        manualInputCard.setVisibility(View.GONE);
        
        // Initialize code scanner
        codeScanner = new CodeScanner(requireContext(), scannerView);
        codeScanner.setDecodeCallback(result -> {
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    processScannedCode(result.getText());
                });
            }
        });
        
        // Start preview
        codeScanner.startPreview();
        isScannerActive = true;
    }
    
    private void stopScanner() {
        if (codeScanner != null) {
            codeScanner.releaseResources();
            codeScanner = null;
        }
        
        // Hide scanner and show other options
        scannerContainer.removeAllViews();
        scannerContainer.setVisibility(View.GONE);
        scanButton.setText("Start Scanning");
        separatorText.setVisibility(View.VISIBLE);
        manualInputCard.setVisibility(View.VISIBLE);
        
        isScannerActive = false;
    }

    private void processScannedCode(String code) {
        // Stop scanning after successful scan
        stopScanner();
        
        // Process the QR code with the repository
        qrCodeRepository.processQRCode(code, new QRCodeRepository.QRCodeOperationCallback() {
            @Override
            public void onComplete(boolean success, String message) {
                if (success) {
                    showResult("Success! " + message);
                    loadUserPoints(); // Refresh points display
                } else {
                    showResult("Error: " + message);
                }
            }
        });
    }

    private void processManualCode(String code) {
        // Hide keyboard and clear input
        codeInput.setText("");
        
        // Process the manually entered code
        qrCodeRepository.processQRCode(code, new QRCodeRepository.QRCodeOperationCallback() {
            @Override
            public void onComplete(boolean success, String message) {
                if (success) {
                    showResult("Success! " + message);
                    loadUserPoints(); // Refresh points display
                } else {
                    showResult("Error: " + message);
                }
            }
        });
    }
    
    private void showResult(String message) {
        resultText.setText(message);
        resultCard.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanner();
            } else {
                Toast.makeText(requireContext(), "Camera permission is required for scanning", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isScannerActive && codeScanner != null) {
            codeScanner.startPreview();
        }
    }

    @Override
    public void onPause() {
        if (codeScanner != null) {
            codeScanner.releaseResources();
        }
        super.onPause();
    }
    
    @Override
    public void onDestroyView() {
        stopScanner();
        super.onDestroyView();
    }
} 