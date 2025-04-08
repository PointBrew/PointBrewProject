package com.example.pointbrewproject.ui.auth;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pointbrewproject.data.model.LoginResult;
import com.example.pointbrewproject.data.repository.UserRepository;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class SignUpViewModel extends ViewModel {

    private static final String TAG = "SignUpViewModel";
    private final UserRepository userRepository;
    private final MutableLiveData<LoginResult> signUpResult = new MutableLiveData<>();

    public SignUpViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LiveData<LoginResult> getSignUpResult() {
        return signUpResult;
    }

    public void register(String fullName, String email, String password, String dob, String phone) {
        // Basic validation
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            signUpResult.setValue(new LoginResult(false, "Name, email, and password are required"));
            return;
        }

        // Validate email format
        if (!isEmailValid(email)) {
            signUpResult.setValue(new LoginResult(false, "Invalid email format"));
            return;
        }

        // Validate password strength
        if (password.length() < 6) {
            signUpResult.setValue(new LoginResult(false, "Password must be at least 6 characters"));
            return;
        }

        // Attempt registration through repository with full profile
        userRepository.registerWithProfile(fullName, email, password, dob, phone, 
            (success, message) -> {
                if (success) {
                    signUpResult.postValue(new LoginResult(true, null));
                } else {
                    signUpResult.postValue(new LoginResult(false, message));
                }
            });
    }
    
    public void signUpWithGoogle(GoogleSignInAccount account) {
        if (account != null) {
            String idToken = account.getIdToken();
            
            if (idToken == null) {
                signUpResult.setValue(new LoginResult(false, "Google sign in failed. Please try again."));
                return;
            }
            
            userRepository.loginWithGoogle(idToken, 
                (success, message) -> {
                    if (success) {
                        signUpResult.postValue(new LoginResult(true, null));
                    } else {
                        signUpResult.postValue(new LoginResult(false, message));
                    }
                });
        } else {
            signUpResult.setValue(new LoginResult(false, "Google sign in failed"));
        }
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
} 