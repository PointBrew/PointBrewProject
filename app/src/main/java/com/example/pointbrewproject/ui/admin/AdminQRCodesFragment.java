package com.example.pointbrewproject.ui.admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.QRCode;
import com.example.pointbrewproject.data.repository.QRCodeRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdminQRCodesFragment extends Fragment implements QRCodeAdapter.OnQRCodeClickListener {
    private RecyclerView qrCodesRecyclerView;
    private QRCodeAdapter adapter;
    private List<QRCode> qrCodes = new ArrayList<>();
    private QRCodeRepository qrCodeRepository;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        qrCodeRepository = QRCodeRepository.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_qr_codes, container, false);

        qrCodesRecyclerView = view.findViewById(R.id.qr_codes_recycler_view);
        qrCodesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new QRCodeAdapter(qrCodes, this);
        qrCodesRecyclerView.setAdapter(adapter);

        FloatingActionButton fabGenerateQR = view.findViewById(R.id.fab_generate_qr);
        fabGenerateQR.setOnClickListener(v -> showQRCodeDialog(null));

        loadQRCodes();

        return view;
    }

    private void loadQRCodes() {
        if (getContext() == null) return;
        
        qrCodeRepository.getAllQRCodes(new QRCodeRepository.QRCodeCallback() {
            @Override
            public void onComplete(List<QRCode> qrCodeList) {
                if (getContext() == null) return;
                
                qrCodes.clear();
                if (qrCodeList != null) {
                    qrCodes.addAll(qrCodeList);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showQRCodeDialog(@Nullable QRCode qrCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_qr_code_form, null);
        builder.setView(dialogView);

        TextInputEditText editPoints = dialogView.findViewById(R.id.edit_points);
        TextInputEditText editMaxScans = dialogView.findViewById(R.id.edit_max_scans);
        TextInputEditText editExpiration = dialogView.findViewById(R.id.edit_expiration);

        if (qrCode != null) {
            editPoints.setText(String.valueOf(qrCode.getPoints()));
            editMaxScans.setText(String.valueOf(qrCode.getMaxScans()));
            editExpiration.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    .format(qrCode.getExpirationDate()));
        }

        editExpiration.setOnClickListener(v -> showDatePicker(editExpiration));

        Dialog dialog = builder.create();
        dialogView.findViewById(R.id.button_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.button_save).setOnClickListener(v -> {
            String pointsStr = editPoints.getText().toString().trim();
            String maxScansStr = editMaxScans.getText().toString().trim();
            String expirationStr = editExpiration.getText().toString().trim();

            if (pointsStr.isEmpty() || maxScansStr.isEmpty() || expirationStr.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int points = Integer.parseInt(pointsStr);
            int maxScans = Integer.parseInt(maxScansStr);
            Calendar calendar = Calendar.getInstance();
            try {
                calendar.setTime(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .parse(expirationStr));
            } catch (Exception e) {
                Toast.makeText(getContext(), "Invalid date format", Toast.LENGTH_SHORT).show();
                return;
            }

            if (qrCode == null) {
                // Generate new QR code
                qrCodeRepository.generateQRCode(points, maxScans, calendar.getTimeInMillis(),
                        "admin", new QRCodeRepository.QRCodeOperationCallback() {
                            @Override
                            public void onComplete(boolean success, String message) {
                                if (success) {
                                    loadQRCodes();
                                    dialog.dismiss();
                                }
                                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // Update existing QR code
                qrCode.setPoints(points);
                qrCode.setMaxScans(maxScans);
                qrCode.setExpirationDate(calendar.getTime());
                qrCodeRepository.updateQRCode(qrCode, new QRCodeRepository.QRCodeOperationCallback() {
                    @Override
                    public void onComplete(boolean success, String message) {
                        if (success) {
                            loadQRCodes();
                            dialog.dismiss();
                        }
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.show();
    }

    private void showDatePicker(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    editText.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            .format(selectedDate.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    @Override
    public void onQRCodeClick(QRCode qrCode) {
        showQRCodeDialog(qrCode);
    }

    @Override
    public void onDeleteClick(QRCode qrCode) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete QR Code")
                .setMessage("Are you sure you want to delete this QR code?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    qrCodeRepository.deleteQRCode(qrCode.getId(), new QRCodeRepository.QRCodeOperationCallback() {
                        @Override
                        public void onComplete(boolean success, String message) {
                            if (success) {
                                loadQRCodes();
                            }
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    @Override
    public void onShareClick(QRCode qrCode, Bitmap qrBitmap) {
        if (qrBitmap == null) {
            Toast.makeText(getContext(), "QR Code is not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            // First save the bitmap to the device
            String fileName = "qrcode_" + qrCode.getCode() + ".png";
            
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PointBrew");
            
            Uri imageUri = null;
            Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            
            imageUri = getContext().getContentResolver().insert(contentUri, values);
            if (imageUri == null) {
                throw new Exception("Failed to create a new MediaStore record");
            }
            
            try (OutputStream os = getContext().getContentResolver().openOutputStream(imageUri)) {
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            }
            
            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.putExtra(Intent.EXTRA_TEXT, 
                    "Scan this QR code to earn " + qrCode.getPoints() + " points!\n" +
                    "Valid until: " + new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            .format(qrCode.getExpirationDate()));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // Start the sharing activity
            startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to share QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 