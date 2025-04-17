package com.example.pointbrewproject.ui.auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.pointbrewproject.MainActivity;
import com.example.pointbrewproject.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";
    private SignUpViewModel viewModel;
    private TextInputEditText firstNameEditText;
    private TextInputEditText lastNameEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText dobEditText;
    private TextInputEditText phoneEditText;
    private TextInputEditText passwordEditText;
    private Button signUpButton;
    private TextView loginTextView;
    private ImageButton togglePasswordVisibility;
    private ImageView backButton;
    private ImageView calendarIcon;
    private View countryCodeContainer;
    private CircularProgressIndicator progressIndicator;
    private GoogleSignInClient googleSignInClient;
    
    private boolean passwordVisible = false;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Handle the result of the Google Sign-In
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        // Google Sign In was successful, authenticate with Firebase
                        viewModel.signUpWithGoogle(account);
                    }
                } catch (ApiException e) {
                    // Google Sign In failed
                    Log.w(TAG, "Google sign in failed", e);
                    Toast.makeText(this, "Google sign in failed: " + e.getStatusCode(), 
                            Toast.LENGTH_SHORT).show();
                    showLoading(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize calendar for date picker
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this, new SignUpViewModelFactory())
                .get(SignUpViewModel.class);

        // Initialize UI components
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        dobEditText = findViewById(R.id.dobEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        loginTextView = findViewById(R.id.loginTextView);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        backButton = findViewById(R.id.backButton);
        calendarIcon = findViewById(R.id.calendarIcon);
        countryCodeContainer = findViewById(R.id.countryCodeContainer);
        progressIndicator = findViewById(R.id.progressIndicator);
        
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set up listeners
        signUpButton.setOnClickListener(v -> attemptSignUp());
        loginTextView.setOnClickListener(v -> navigateToLogin());
        backButton.setOnClickListener(v -> onBackPressed());
        togglePasswordVisibility.setOnClickListener(v -> togglePasswordVisibility());
        
        // Set up date picker
        View.OnClickListener showDatePicker = v -> showDatePickerDialog();
        dobEditText.setOnClickListener(showDatePicker);
        calendarIcon.setOnClickListener(showDatePicker);
        
        // Set up country code selector
        countryCodeContainer.setOnClickListener(v -> showCountryCodePicker());

        // Observe registration result
        viewModel.getSignUpResult().observe(this, signUpResult -> {
            showLoading(false);
            
            if (signUpResult.isSuccess()) {
                // Navigate to main activity
                Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                // Show error message
                Toast.makeText(SignUpActivity.this, 
                        signUpResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptSignUp() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String fullName = firstName + " " + lastName;
        String email = emailEditText.getText().toString().trim();
        String dob = dobEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        
        // Basic validation
        if (firstName.isEmpty()) {
            firstNameEditText.setError("First name is required");
            return;
        }
        
        if (lastName.isEmpty()) {
            lastNameEditText.setError("Last name is required");
            return;
        }
        
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            return;
        }
        
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            return;
        }
        
        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            return;
        }

        showLoading(true);
        viewModel.register(fullName, email, password, dob, phone);
    }
    
    private void signInWithGoogle() {
        showLoading(true);
        // Sign out first to force the account picker to show
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void navigateToLogin() {
        // Navigate to login screen
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
    
    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateInView();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void updateDateInView() {
        dobEditText.setText(dateFormat.format(calendar.getTime()));
    }
    
    private void showCountryCodePicker() {
        // In a real app, this would show a country code picker dialog/fragment
        Toast.makeText(this, "Country code picker would be shown here", Toast.LENGTH_SHORT).show();
    }

    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            // Show password
            passwordEditText.setTransformationMethod(null);
            togglePasswordVisibility.setImageResource(R.drawable.ic_visibility_off);
        } else {
            // Hide password
            passwordEditText.setTransformationMethod(new PasswordTransformationMethod());
            togglePasswordVisibility.setImageResource(R.drawable.ic_visibility);
        }
        passwordEditText.setSelection(passwordEditText.getText().length());
    }
    
    private void showLoading(boolean isLoading) {
        progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        signUpButton.setEnabled(!isLoading);
    }
} 