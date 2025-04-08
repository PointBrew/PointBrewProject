package com.example.pointbrewproject.ui.auth;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pointbrewproject.data.model.LoginResult;
import com.example.pointbrewproject.data.repository.UserRepository;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class LoginViewModel extends ViewModel {

    private static final String TAG = "LoginViewModel";
    private final UserRepository userRepository;
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();

    public LoginViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String email, String password, boolean rememberMe) {
        // Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            loginResult.setValue(new LoginResult(false, "Email and password cannot be empty"));
            return;
        }

        // Add more validation as needed
        if (!isEmailValid(email)) {
            loginResult.setValue(new LoginResult(false, "Invalid email format"));
            return;
        }

        // Attempt login through repository
        userRepository.login(email, password, rememberMe, 
            (success, message) -> {
                if (success) {
                    loginResult.postValue(new LoginResult(true, null));
                } else {
                    loginResult.postValue(new LoginResult(false, message));
                }
            });
    }

    public void handleGoogleSignInResult(GoogleSignInAccount account) {
        if (account != null) {
            String idToken = account.getIdToken();
            
            userRepository.loginWithGoogle(idToken, 
                (success, message) -> {
                    if (success) {
                        loginResult.postValue(new LoginResult(true, null));
                    } else {
                        loginResult.postValue(new LoginResult(false, message));
                    }
                });
        } else {
            loginResult.setValue(new LoginResult(false, "Google sign in failed"));
        }
    }
    
    public void resetPassword(String email) {
        if (email.isEmpty()) {
            loginResult.setValue(new LoginResult(false, "Email cannot be empty"));
            return;
        }
        
        if (!isEmailValid(email)) {
            loginResult.setValue(new LoginResult(false, "Invalid email format"));
            return;
        }
        
        userRepository.resetPassword(email, (success, message) -> {
            loginResult.postValue(new LoginResult(success, message));
        });
    }

    private boolean isEmailValid(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
} 