package com.example.pointbrewproject.ui.admin;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.model.QRCode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QRCodeAdapter extends RecyclerView.Adapter<QRCodeAdapter.QRCodeViewHolder> {
    private final List<QRCode> qrCodes;
    private final OnQRCodeClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private final ExecutorService qrExecutor = Executors.newFixedThreadPool(2);

    public interface OnQRCodeClickListener {
        void onQRCodeClick(QRCode qrCode);
        void onDeleteClick(QRCode qrCode);
        void onShareClick(QRCode qrCode, Bitmap qrBitmap);
    }

    public QRCodeAdapter(List<QRCode> qrCodes, OnQRCodeClickListener listener) {
        this.qrCodes = qrCodes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QRCodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_qr_code, parent, false);
        return new QRCodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QRCodeViewHolder holder, int position) {
        QRCode qrCode = qrCodes.get(position);
        holder.bind(qrCode, listener, qrExecutor);
    }

    @Override
    public int getItemCount() {
        return qrCodes.size();
    }

    public void updateQRCodes(List<QRCode> newQRCodes) {
        qrCodes.clear();
        qrCodes.addAll(newQRCodes);
        notifyDataSetChanged();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        qrExecutor.shutdown();
    }

    static class QRCodeViewHolder extends RecyclerView.ViewHolder {
        private final TextView codeText;
        private final TextView pointsText;
        private final TextView scansText;
        private final TextView expirationText;
        private final ImageView qrImageView;
        private final ProgressBar progressBar;
        private final SimpleDateFormat dateFormat;
        private Bitmap qrBitmap;

        public QRCodeViewHolder(@NonNull View itemView) {
            super(itemView);
            codeText = itemView.findViewById(R.id.text_qr_code);
            pointsText = itemView.findViewById(R.id.text_points);
            scansText = itemView.findViewById(R.id.text_scans);
            expirationText = itemView.findViewById(R.id.text_expiration);
            qrImageView = itemView.findViewById(R.id.image_qr_code);
            progressBar = itemView.findViewById(R.id.progress_qr_code);
            dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        }

        public void bind(QRCode qrCode, OnQRCodeClickListener listener, ExecutorService executor) {
            codeText.setText(qrCode.getCode());
            pointsText.setText(String.format("Points: %d", qrCode.getPoints()));
            scansText.setText(String.format("Scans: %d/%d", qrCode.getCurrentScans(), qrCode.getMaxScans()));
            expirationText.setText(String.format("Expires: %s", dateFormat.format(qrCode.getExpirationDate())));

            itemView.findViewById(R.id.button_edit).setOnClickListener(v -> listener.onQRCodeClick(qrCode));
            itemView.findViewById(R.id.button_delete).setOnClickListener(v -> listener.onDeleteClick(qrCode));
            itemView.findViewById(R.id.button_share).setOnClickListener(v -> {
                if (qrBitmap != null) {
                    listener.onShareClick(qrCode, qrBitmap);
                } else {
                    Toast.makeText(itemView.getContext(), "QR Code is still generating", Toast.LENGTH_SHORT).show();
                }
            });

            // Generate QR code in background
            qrImageView.setImageBitmap(null);
            progressBar.setVisibility(View.VISIBLE);
            
            executor.execute(() -> {
                try {
                    qrBitmap = generateQRCode(qrCode.getCode(), itemView.getContext());
                    
                    // Update UI on main thread
                    itemView.post(() -> {
                        if (qrBitmap != null) {
                            qrImageView.setImageBitmap(qrBitmap);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                } catch (Exception e) {
                    itemView.post(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(itemView.getContext(), "Failed to generate QR code", Toast.LENGTH_SHORT).show();
                    });
                }
            });
        }
        
        private Bitmap generateQRCode(String content, Context context) {
            try {
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                BitMatrix bitMatrix = multiFormatWriter.encode(
                        content,
                        BarcodeFormat.QR_CODE,
                        500,
                        500
                );
                
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                return barcodeEncoder.createBitmap(bitMatrix);
            } catch (WriterException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
} 