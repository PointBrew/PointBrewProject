package com.example.pointbrewproject.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.pointbrewproject.R;
import com.example.pointbrewproject.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private LoginViewModel loginViewModel;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button googleSignInButton;
    private CheckBox rememberMeCheckbox;
    private TextView forgotPasswordTextView;
    private TextView signUpTextView;
    private ImageButton togglePasswordVisibility;
    private CircularProgressIndicator progressIndicator;
    private GoogleSignInClient googleSignInClient;
    private boolean passwordVisible = false;
    private FirebaseAuth firebaseAuth;
    
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Handle the result of the Google Sign-In
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null) {
                        // Google Sign In was successful, authenticate with Firebase
                        loginViewModel.handleGoogleSignInResult(account);
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
        setContentView(R.layout.activity_login);
        
        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize ViewModel
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        // Initialize UI components
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        googleSignInButton = findViewById(R.id.googleSignInButton);
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        signUpTextView = findViewById(R.id.signUpTextView);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        progressIndicator = findViewById(R.id.progressIndicator);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set up listeners
        loginButton.setOnClickListener(v -> attemptLogin());
        googleSignInButton.setOnClickListener(v -> signInWithGoogle());
        forgotPasswordTextView.setOnClickListener(v -> navigateToForgotPassword());
        signUpTextView.setOnClickListener(v -> navigateToSignUp());
        togglePasswordVisibility.setOnClickListener(v -> togglePasswordVisibility());

        // Observe login result
        loginViewModel.getLoginResult().observe(this, loginResult -> {
            showLoading(false);
            
            if (loginResult.isSuccess()) {
                // Navigate to main activity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                // Show error message
                Toast.makeText(LoginActivity.this, 
                        loginResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is already signed in
        if (firebaseAuth.getCurrentUser() != null) {
            // User is already signed in, go to MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        boolean rememberMe = rememberMeCheckbox.isChecked();

        // Basic validation
        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            return;
        }
        
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            return;
        }
        
        showLoading(true);
        loginViewModel.login(email, password, rememberMe);
    }

    private void signInWithGoogle() {
        showLoading(true);
        // Sign out first to force the account picker to show
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void navigateToSignUp() {
        // Navigate to sign up screen
        startActivity(new Intent(this, SignUpActivity.class));
    }

    private void navigateToForgotPassword() {
        startActivity(new Intent(this, ForgotPasswordActivity.class));
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
        loginButton.setEnabled(!isLoading);
        googleSignInButton.setEnabled(!isLoading);
    }
} 