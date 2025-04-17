package com.example.pointbrewproject.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.pointbrewproject.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.FirebaseNetworkException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for handling user authentication and user data using Firebase
 */
public class UserRepository {
    
    private static final String TAG = "UserRepository";
    private static volatile UserRepository instance;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    
    // Singleton pattern
    public static UserRepository getInstance() {
        if (instance == null) {
            synchronized (UserRepository.class) {
                if (instance == null) {
                    instance = new UserRepository();
                }
            }
        }
        return instance;
    }
    
    private UserRepository() {
        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }
    
    public interface AuthCallback {
        void onComplete(boolean success, String message);
    }
    
    // Hardcoded admin credentials 
    private static final String ADMIN_EMAIL = "admin@gmail.com";
    private static final String ADMIN_PASSWORD = "admin123";
    
    /**
     * Login with email and password
     */
    public void login(String email, String password, boolean rememberMe, AuthCallback callback) {
        // Check if trying to login with admin credentials
        if (ADMIN_EMAIL.equals(email) && ADMIN_PASSWORD.equals(password)) {
            // Special case for admin login
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Admin sign in success
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                // Ensure user has admin role in Firestore
                                ensureAdminRole(firebaseUser.getUid(), callback);
                            } else {
                                callback.onComplete(false, "Authentication failed");
                            }
                        } else {
                            Exception exception = task.getException();
                            if (exception instanceof FirebaseNetworkException) {
                                // Retry once on network error
                                retryLogin(email, password, callback);
                            } else {
                                // If admin account doesn't exist yet, create it
                                createAdminAccount(email, password, callback);
                            }
                        }
                    });
            return;
        }
        
        // Regular user login
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            callback.onComplete(true, null);
                        } else {
                            callback.onComplete(false, "Authentication failed");
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseNetworkException) {
                            // Retry once on network error
                            retryLogin(email, password, callback);
                        } else {
                            // If sign in fails, display a message to the user
                            Log.w(TAG, "signInWithEmail:failure", exception);
                            callback.onComplete(false, "Authentication failed: " + 
                                    (exception != null ? exception.getMessage() : "Unknown error"));
                        }
                    }
                });
    }
    
    private void retryLogin(String email, String password, AuthCallback callback) {
        // Wait for 2 seconds before retrying
        new android.os.Handler().postDelayed(() -> {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                if (ADMIN_EMAIL.equals(email)) {
                                    ensureAdminRole(firebaseUser.getUid(), callback);
                                } else {
                                    callback.onComplete(true, null);
                                }
                            } else {
                                callback.onComplete(false, "Authentication failed");
                            }
                        } else {
                            Log.w(TAG, "retryLogin:failure", task.getException());
                            callback.onComplete(false, "Network error. Please check your internet connection and try again.");
                        }
                    });
        }, 2000);
    }
    
    /**
     * Create admin account if it doesn't exist
     */
    private void createAdminAccount(String email, String password, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Admin account creation success
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Save admin data to Firestore
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("email", email);
                            userData.put("fullName", "Admin");
                            userData.put("role", "admin");
                            
                            firestore.collection("users").document(firebaseUser.getUid())
                                    .set(userData)
                                    .addOnSuccessListener(aVoid -> callback.onComplete(true, null))
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error writing admin document", e);
                                        callback.onComplete(false, "Failed to save admin data");
                                    });
                        } else {
                            callback.onComplete(false, "Admin account creation failed");
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof FirebaseNetworkException) {
                            // Retry once on network error
                            retryCreateAdmin(email, password, callback);
                        } else {
                            Log.w(TAG, "createAdminWithEmail:failure", exception);
                            callback.onComplete(false, "Admin account creation failed: " + 
                                    (exception != null ? exception.getMessage() : "Unknown error"));
                        }
                    }
                });
    }

    private void retryCreateAdmin(String email, String password, AuthCallback callback) {
        // Wait for 2 seconds before retrying
        new android.os.Handler().postDelayed(() -> {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("email", email);
                                userData.put("fullName", "Admin");
                                userData.put("role", "admin");
                                
                                firestore.collection("users").document(firebaseUser.getUid())
                                        .set(userData)
                                        .addOnSuccessListener(aVoid -> callback.onComplete(true, null))
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error writing admin document on retry", e);
                                            callback.onComplete(false, "Failed to save admin data");
                                        });
                            } else {
                                callback.onComplete(false, "Admin account creation failed");
                            }
                        } else {
                            Log.w(TAG, "retryCreateAdmin:failure", task.getException());
                            callback.onComplete(false, "Network error. Please check your internet connection and try again.");
                        }
                    });
        }, 2000);
    }
    
    /**
     * Ensure the user has admin role in Firestore
     */
    private void ensureAdminRole(String userId, AuthCallback callback) {
        DocumentReference userRef = firestore.collection("users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Check if role is admin
                    String role = document.getString("role");
                    if ("admin".equals(role)) {
                        // Already has admin role
                        callback.onComplete(true, null);
                    } else {
                        // Update role to admin
                        userRef.update("role", "admin")
                                .addOnSuccessListener(aVoid -> callback.onComplete(true, null))
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating admin role", e);
                                    callback.onComplete(false, "Failed to update admin role");
                                });
                    }
                } else {
                    // Document doesn't exist, create it
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("email", ADMIN_EMAIL);
                    userData.put("fullName", "Admin");
                    userData.put("role", "admin");
                    
                    userRef.set(userData)
                            .addOnSuccessListener(aVoid -> callback.onComplete(true, null))
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error creating admin document", e);
                                callback.onComplete(false, "Failed to create admin data");
                            });
                }
            } else {
                Log.e(TAG, "Error checking admin role", task.getException());
                callback.onComplete(false, "Failed to check admin role");
            }
        });
    }
    
    /**
     * Login with Google by using the ID token
     */
    public void loginWithGoogle(String idToken, AuthCallback callback) {
        if (idToken == null) {
            callback.onComplete(false, "Google Sign-In failed. Try again.");
            return;
        }
        
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                            if (isNewUser) {
                                // This is a new user, save additional data to Firestore
                                saveUserDataToFirestore(firebaseUser, null, null, null, callback);
                            } else {
                                // Existing user, just return success
                                callback.onComplete(true, null);
                            }
                        } else {
                            callback.onComplete(false, "Authentication failed");
                        }
                    } else {
                        // If sign in fails, display a message to the user
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        callback.onComplete(false, "Google authentication failed: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }
    
    /**
     * Link Google account with existing email account
     */
    public void linkGoogleWithEmail(String idToken, AuthCallback callback) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            callback.onComplete(false, "User not signed in");
            return;
        }
        
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        user.linkWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onComplete(true, "Google account linked successfully");
                    } else {
                        Log.w(TAG, "linkWithCredential:failure", task.getException());
                        callback.onComplete(false, "Failed to link Google account: " + 
                                (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                    }
                });
    }
    
    /**
     * Register a new user with email and password
     */
    public void register(String email, String password, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Registration success
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Just return success, the user data will be saved in registerWithProfile
                            callback.onComplete(true, null);
                        } else {
                            callback.onComplete(false, "Registration failed");
                        }
                    } else {
                        // If registration fails, display a message to the user
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            callback.onComplete(false, "Email already exists. Please use a different email or sign in.");
                        } else {
                            callback.onComplete(false, "Registration failed: " + 
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        }
                    }
                });
    }
    
    /**
     * Register a new user with additional profile information
     */
    public void registerWithProfile(String fullName, String email, String password, String dob, String phone, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Registration success
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Save additional user data to Firestore
                            saveUserDataToFirestore(firebaseUser, fullName, dob, phone, callback);
                        } else {
                            callback.onComplete(false, "Registration failed");
                        }
                    } else {
                        // If registration fails, display a message to the user
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            // The email already exists, let's check if it's a Google account
                            checkIfGoogleAccount(email, password, fullName, dob, phone, callback);
                        } else {
                            callback.onComplete(false, "Registration failed: " + 
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        }
                    }
                });
    }
    
    /**
     * Check if an email already exists as a Google account
     */
    private void checkIfGoogleAccount(String email, String password, String fullName, String dob, String phone, AuthCallback callback) {
        firebaseAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        try {
                            boolean isGoogleAccount = false;
                            for (String method : task.getResult().getSignInMethods()) {
                                if (method.equals(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD)) {
                                    isGoogleAccount = true;
                                    break;
                                }
                            }
                            
                            if (isGoogleAccount) {
                                callback.onComplete(false, "This email is already registered with Google. Please use Google Sign-In.");
                            } else {
                                callback.onComplete(false, "Email already exists. Please use a different email or sign in.");
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error checking sign-in methods", e);
                            callback.onComplete(false, "Email already exists. Please use a different email or sign in.");
                        }
                    } else {
                        Log.w(TAG, "fetchSignInMethodsForEmail:failure", task.getException());
                        callback.onComplete(false, "Email already exists. Please use a different email or sign in.");
                    }
                });
    }
    
    /**
     * Save additional user data to Firestore
     */
    private void saveUserDataToFirestore(FirebaseUser firebaseUser, String fullName, String dob, String phone, AuthCallback callback) {
        // Create a new user document in Firestore
        DocumentReference userRef = firestore.collection("users").document(firebaseUser.getUid());
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", firebaseUser.getEmail());
        
        // For Google Sign-In, use the display name if available
        if (fullName == null && firebaseUser.getDisplayName() != null && !firebaseUser.getDisplayName().isEmpty()) {
            userData.put("fullName", firebaseUser.getDisplayName());
        } else if (fullName != null) {
            userData.put("fullName", fullName);
        }
        
        // Add profile picture URL if available from Google
        if (firebaseUser.getPhotoUrl() != null) {
            userData.put("profilePictureUrl", firebaseUser.getPhotoUrl().toString());
        }
        
        // Add date of birth and phone if available
        if (dob != null && !dob.isEmpty()) {
            userData.put("dateOfBirth", dob);
        }
        
        if (phone != null && !phone.isEmpty()) {
            userData.put("phone", phone);
        }
        
        // Set default role to "user"
        userData.put("role", "user");
        
        // Add timestamp for when the account was created
        userData.put("createdAt", com.google.firebase.Timestamp.now());
        
        userRef.set(userData)
                .addOnSuccessListener(aVoid -> {
                    // Data saved successfully
                    callback.onComplete(true, null);
                })
                .addOnFailureListener(e -> {
                    // Error saving data
                    Log.e(TAG, "Error saving user data", e);
                    callback.onComplete(false, "Registration successful but failed to save profile data. Please update your profile later.");
                });
    }
    
    /**
     * Get user data from Firestore
     */
    public void getUserData(String userId, OnCompleteListener<DocumentSnapshot> listener) {
        firestore.collection("users").document(userId)
                .get()
                .addOnCompleteListener(listener);
    }
    
    /**
     * Logout the current user
     */
    public void logout() {
        firebaseAuth.signOut();
    }
    
    /**
     * Get the current FirebaseUser
     */
    public FirebaseUser getCurrentFirebaseUser() {
        return firebaseAuth.getCurrentUser();
    }
    
    /**
     * Get the current User model
     */
    public void getCurrentUser(OnCompleteListener<User> listener) {
        FirebaseUser firebaseUser = getCurrentFirebaseUser();
        if (firebaseUser == null) {
            listener.onComplete(Tasks.forCanceled());
            return;
        }

        firestore.collection("users").document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String email = document.getString("email");
                            String fullName = document.getString("fullName");
                            String profilePictureUrl = document.getString("profilePictureUrl");
                            String role = document.getString("role");
                            Integer points = document.getLong("points") != null ? document.getLong("points").intValue() : 0;

                            User user = new User(firebaseUser.getUid(), email, fullName, profilePictureUrl, role);
                            user.setPoints(points);
                            listener.onComplete(Tasks.forResult(user));
                        } else {
                            listener.onComplete(Tasks.forCanceled());
                        }
                    } else {
                        listener.onComplete(Tasks.forCanceled());
                    }
                });
    }
    
    /**
     * Check if a user is currently logged in
     */
    public boolean isLoggedIn() {
        return getCurrentFirebaseUser() != null;
    }
    
    /**
     * Send password reset email
     */
    public void resetPassword(String email, AuthCallback callback) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> callback.onComplete(true, "Reset password email sent"))
                .addOnFailureListener(e -> callback.onComplete(false, "Failed to send reset email: " + e.getMessage()));
    }

    public void updateUser(User user, OnCompleteListener<Boolean> listener) {
        if (user == null || user.getId() == null) {
            listener.onComplete(Tasks.forResult(false));
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("fullName", user.getName());
        userData.put("profilePictureUrl", user.getProfilePictureUrl());
        userData.put("role", user.getRole());
        userData.put("points", user.getPoints());

        firestore.collection("users").document(user.getId())
                .set(userData)
                .addOnCompleteListener(task -> {
                    listener.onComplete(Tasks.forResult(task.isSuccessful()));
                });
    }

    public void createAdminUserIfNotExists() {
        firebaseAuth.signInWithEmailAndPassword(ADMIN_EMAIL, ADMIN_PASSWORD)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        // Admin user doesn't exist, create it
                        firebaseAuth.createUserWithEmailAndPassword(ADMIN_EMAIL, ADMIN_PASSWORD)
                                .addOnCompleteListener(createTask -> {
                                    if (createTask.isSuccessful()) {
                                        User adminUser = new User();
                                        adminUser.setId(createTask.getResult().getUser().getUid());
                                        adminUser.setEmail(ADMIN_EMAIL);
                                        adminUser.setName("Admin");
                                        adminUser.setDisplayName("Admin");
                                        adminUser.setRole("admin");
                                        adminUser.setAdmin(true);
                                        firestore.collection("users").document(adminUser.getId())
                                                .set(adminUser);
                                    }
                                });
                    }
                });
    }

    /**
     * Add points to a user's account
     * @param userId The ID of the user
     * @param points The number of points to add
     * @param callback Callback to notify of completion
     */
    public void addPoints(String userId, int points, QRCodeRepository.QRCodeOperationCallback callback) {
        DocumentReference userRef = firestore.collection("users").document(userId);
        
        firestore.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);
            long currentPoints = 0;
            if (snapshot.contains("points")) {
                currentPoints = snapshot.getLong("points") != null ? snapshot.getLong("points") : 0;
            }
            
            long newPoints = currentPoints + points;
            transaction.update(userRef, "points", newPoints);
            return null;
        })
        .addOnSuccessListener(aVoid -> callback.onComplete(true, "Successfully added " + points + " points!"))
        .addOnFailureListener(e -> callback.onComplete(false, "Failed to add points: " + e.getMessage()));
    }

    /**
     * Get a list of all users from Firestore
     */
    public void getAllUsers(OnCompleteListener<List<User>> listener) {
        firestore.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            String id = document.getId();
                            String email = document.getString("email");
                            String fullName = document.getString("fullName");
                            String profilePictureUrl = document.getString("profilePictureUrl");
                            String role = document.getString("role");
                            Integer points = document.getLong("points") != null 
                                    ? document.getLong("points").intValue() : 0;

                            User user = new User(id, email, fullName, profilePictureUrl, role);
                            user.setPoints(points);
                            users.add(user);
                        }
                        listener.onComplete(Tasks.forResult(users));
                    } else {
                        listener.onComplete(Tasks.forException(
                                task.getException() != null ? 
                                task.getException() : 
                                new Exception("Failed to get users")));
                    }
                });
    }
} 