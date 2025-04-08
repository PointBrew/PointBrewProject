package com.example.pointbrewproject.ui.points;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.example.pointbrewproject.R;

public class ScannerActivity extends AppCompatActivity {
    
    private CodeScanner codeScanner;
    private Button cancelButton;
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        
        // Initialize scanner view
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        TextView scanInstructionsText = findViewById(R.id.scan_instructions);
        cancelButton = findViewById(R.id.cancel_button);
        
        // Set up scanner
        codeScanner = new CodeScanner(this, scannerView);
        codeScanner.setDecodeCallback(result -> {
            runOnUiThread(() -> {
                // Return scanned result to calling activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("SCANNED_CODE", result.getText());
                setResult(RESULT_OK, resultIntent);
                finish();
            });
        });
        
        // Set up cancel button
        cancelButton.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        
        // Apply scanner settings for better performance
        scannerView.setAutoFocusButtonVisible(true);
        scannerView.setFlashButtonVisible(true);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        codeScanner.startPreview();
    }
    
    @Override
    protected void onPause() {
        codeScanner.releaseResources();
        super.onPause();
    }
} 