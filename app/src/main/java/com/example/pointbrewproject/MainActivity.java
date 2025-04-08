package com.example.pointbrewproject;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.pointbrewproject.data.model.User;
import com.example.pointbrewproject.data.repository.UserRepository;
import com.example.pointbrewproject.ui.admin.AdminDashboardFragment;
import com.example.pointbrewproject.ui.admin.ManageUsersFragment;
import com.example.pointbrewproject.ui.admin.AdminRewardsFragment;
import com.example.pointbrewproject.ui.admin.AdminQRCodesFragment;
import com.example.pointbrewproject.ui.explore.ExploreFragment;
import com.example.pointbrewproject.ui.favorites.FavoritesFragment;
import com.example.pointbrewproject.ui.home.HomeFragment;
import com.example.pointbrewproject.ui.points.PointsFragment;
import com.example.pointbrewproject.ui.profile.ProfileFragment;
import com.example.pointbrewproject.ui.rewards.RewardsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.gms.tasks.Tasks;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private BottomNavigationView bottomNavigationView;
    private UserRepository userRepository;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize UserRepository
        userRepository = UserRepository.getInstance();
        
        // Initialize UI components
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        
        // Check if user is admin
        checkUserRole();
    }
    
    private void checkUserRole() {
        userRepository.getCurrentUser(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                User user = task.getResult();
                isAdmin = user.isAdmin();
                
                // Set up navigation menu based on role
                setupNavigation();
                
                // Load initial fragment
                if (isAdmin) {
                    loadFragment(new AdminDashboardFragment());
                    bottomNavigationView.setSelectedItemId(R.id.navigation_admin);
                } else {
                    loadFragment(new HomeFragment());
                    bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                }
            } else {
                // Default to regular user if we can't get role
                setupNavigation();
                loadFragment(new HomeFragment());
                bottomNavigationView.setSelectedItemId(R.id.navigation_home);
            }
        });
    }
    
    private void setupNavigation() {
        if (isAdmin) {
            // Show admin navigation menu
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_admin);
            bottomNavigationView.setItemIconTintList(getColorStateList(R.color.nav_item_admin_color));
            bottomNavigationView.setItemTextColor(getColorStateList(R.color.nav_item_admin_color));
        } else {
            // Show regular user navigation menu
            bottomNavigationView.getMenu().clear();
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);
            bottomNavigationView.setItemIconTintList(getColorStateList(R.color.nav_item_color));
            bottomNavigationView.setItemTextColor(getColorStateList(R.color.nav_item_color));
        }
        
        // Add elevation animation when scrolling
        bottomNavigationView.setElevation(8f);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();
        
        if (isAdmin) {
            // Admin navigation
            if (itemId == R.id.navigation_admin) {
                selectedFragment = new AdminDashboardFragment();
            } else if (itemId == R.id.navigation_admin_rewards) {
                selectedFragment = new AdminRewardsFragment();
            } else if (itemId == R.id.navigation_manage_users) {
                selectedFragment = new ManageUsersFragment();
            } else if (itemId == R.id.navigation_qr_code) {
                selectedFragment = new AdminQRCodesFragment();
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            }
        } else {
            // Regular user navigation
            if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.navigation_points) {
                selectedFragment = new PointsFragment();
            } else if (itemId == R.id.navigation_rewards) {
                selectedFragment = new RewardsFragment();
            } else if (itemId == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            }
        }
        
        if (selectedFragment != null) {
            return loadFragment(selectedFragment);
        }
        
        return false;
    }
    
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.fade_in,
                            R.anim.fade_out,
                            R.anim.fade_in,
                            R.anim.fade_out
                    )
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}