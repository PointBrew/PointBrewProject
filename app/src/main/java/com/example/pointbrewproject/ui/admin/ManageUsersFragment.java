package com.example.pointbrewproject.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pointbrewproject.R;
import com.example.pointbrewproject.data.repository.UserRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ManageUsersFragment extends Fragment {

    private TextView titleText;
    private RecyclerView usersRecyclerView;
    private TextView emptyView;
    private UserRepository userRepository;
    private FirebaseFirestore firestore;

    public ManageUsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manage_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        userRepository = UserRepository.getInstance();
        firestore = FirebaseFirestore.getInstance();
        
        // Initialize views
        titleText = view.findViewById(R.id.text_manage_users_title);
        usersRecyclerView = view.findViewById(R.id.recycler_users);
        emptyView = view.findViewById(R.id.text_no_users);
        
        // Set up RecyclerView
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Set up user detail view
        View userDetailContainer = view.findViewById(R.id.user_detail_container);
        TextView userDetailName = view.findViewById(R.id.text_user_detail_name);
        View backButton = view.findViewById(R.id.back_button);
        
        // Set up back button click listener
        backButton.setOnClickListener(v -> {
            userDetailContainer.setVisibility(View.GONE);
            usersRecyclerView.setVisibility(View.VISIBLE);
        });
        
        // Load users
        loadUsers();
    }
    
    private void loadUsers() {
        // This is a simplified approach. In a real app, we'd use pagination, search, and filtering
        firestore.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> userList = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Add user data to list
                        userList.add(document.getData());
                    }
                    
                    if (userList.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        usersRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        usersRecyclerView.setVisibility(View.VISIBLE);
                        
                        // Mock data for demo since we might not have real users yet
                        if (userList.size() < 3) {
                            addMockUsers(userList);
                        }
                        
                        // Set adapter
                        UserAdapter adapter = new UserAdapter(userList);
                        adapter.setOnUserClickListener(position -> {
                            Map<String, Object> selectedUser = userList.get(position);
                            showUserDetail(selectedUser);
                        });
                        usersRecyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading users: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }
    
    private void showUserDetail(Map<String, Object> user) {
        if (getView() == null) return;
        
        View userDetailContainer = getView().findViewById(R.id.user_detail_container);
        TextView userDetailName = getView().findViewById(R.id.text_user_detail_name);
        RecyclerView redeemedRewardsRecycler = getView().findViewById(R.id.recycler_user_redeemed_rewards);
        TextView noRedeemedRewardsText = getView().findViewById(R.id.text_no_redeemed_rewards);
        
        // Set user details
        userDetailName.setText((String) user.get("fullName"));
        
        // Set up redeemed rewards recycler
        redeemedRewardsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        
        // Show user detail view
        usersRecyclerView.setVisibility(View.GONE);
        userDetailContainer.setVisibility(View.VISIBLE);
        
        // Get user ID
        String userId = (String) user.get("id");
        if (userId == null && user.containsKey("email")) {
            // Use email as fallback ID for demo
            userId = (String) user.get("email");
        }
        
        // Load user's redeemed rewards
        if (userId != null) {
            loadUserRedeemedRewards(userId, redeemedRewardsRecycler, noRedeemedRewardsText);
        } else {
            // Handle case where user ID is not available
            noRedeemedRewardsText.setVisibility(View.VISIBLE);
            redeemedRewardsRecycler.setVisibility(View.GONE);
        }
    }
    
    private void loadUserRedeemedRewards(String userId, RecyclerView recyclerView, TextView emptyView) {
        FirebaseFirestore.getInstance()
            .collection("redeemedRewards")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Map<String, Object>> redeemedRewards = new ArrayList<>();
                
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    Map<String, Object> rewardData = document.getData();
                    // Add document ID to the map so we can reference it later
                    if (rewardData != null) {
                        rewardData.put("documentId", document.getId());
                        redeemedRewards.add(rewardData);
                    }
                }
                
                if (redeemedRewards.isEmpty()) {
                    // Use mock data for demo if no real data exists
                    addMockRedeemedRewards(redeemedRewards, userId);
                }
                
                if (redeemedRewards.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                    recyclerView.setAdapter(new RedeemedRewardsAdapter(redeemedRewards));
                }
            })
            .addOnFailureListener(e -> {
                // Handle error
                Toast.makeText(getContext(), "Error loading redeemed rewards: " + e.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            });
    }
    
    private void addMockRedeemedRewards(List<Map<String, Object>> rewardsList, String userId) {
        // Add some mock redeemed rewards for demo purposes
        String[] rewardNames = {"Free Coffee", "20% Discount", "Loyalty Item"};
        String[] rewardDates = {"2025-04-10", "2025-04-08", "2025-04-01"};
        boolean[] usedStatus = {false, true, false};
        
        for (int i = 0; i < rewardNames.length; i++) {
            Map<String, Object> reward = new java.util.HashMap<>();
            reward.put("userId", userId);
            reward.put("rewardName", rewardNames[i]);
            reward.put("redeemedDate", rewardDates[i]);
            reward.put("used", usedStatus[i]);
            reward.put("documentId", "mock_" + i);
            rewardsList.add(reward);
        }
    }
    
    // Adapter for redeemed rewards
    private class RedeemedRewardsAdapter extends RecyclerView.Adapter<RedeemedRewardsAdapter.RewardViewHolder> {
        
        private List<Map<String, Object>> rewardsList;
        
        RedeemedRewardsAdapter(List<Map<String, Object>> rewardsList) {
            this.rewardsList = rewardsList;
        }
        
        @NonNull
        @Override
        public RewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_redeemed_reward_admin, parent, false);
            return new RewardViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull RewardViewHolder holder, int position) {
            Map<String, Object> reward = rewardsList.get(position);
            
            holder.rewardNameText.setText((String) reward.get("rewardName"));
            holder.rewardDateText.setText((String) reward.get("redeemedDate"));
            
            // Set used status
            boolean isUsed = reward.containsKey("used") && (boolean) reward.get("used");
            if (isUsed) {
                holder.usedStatusText.setText("Used");
                holder.usedStatusText.setChipBackgroundColorResource(R.color.coffee_light);
                holder.markAsUsedButton.setVisibility(View.GONE);
            } else {
                holder.usedStatusText.setText("Not Used");
                holder.usedStatusText.setChipBackgroundColorResource(R.color.green);
                holder.markAsUsedButton.setVisibility(View.VISIBLE);
            }
            
            // Set up mark as used button
            holder.markAsUsedButton.setOnClickListener(v -> {
                String documentId = (String) reward.get("documentId");
                markRewardAsUsed(documentId, position);
                
                // Update UI immediately (optimistic update)
                reward.put("used", true);
                notifyItemChanged(position);
            });
        }
        
        @Override
        public int getItemCount() {
            return rewardsList.size();
        }
        
        class RewardViewHolder extends RecyclerView.ViewHolder {
            TextView rewardNameText;
            TextView rewardDateText;
            com.google.android.material.chip.Chip usedStatusText;
            com.google.android.material.button.MaterialButton markAsUsedButton;
            
            RewardViewHolder(@NonNull View itemView) {
                super(itemView);
                rewardNameText = itemView.findViewById(R.id.text_reward_name);
                rewardDateText = itemView.findViewById(R.id.text_reward_date);
                usedStatusText = itemView.findViewById(R.id.text_used_status);
                markAsUsedButton = itemView.findViewById(R.id.button_mark_used);
            }
        }
    }
    
    private void markRewardAsUsed(String documentId, int position) {
        // Skip update for mock data
        if (documentId.startsWith("mock_")) {
            Toast.makeText(getContext(), "Reward marked as used (Mock)", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Update in Firestore
        FirebaseFirestore.getInstance()
            .collection("redeemedRewards")
            .document(documentId)
            .update("used", true)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Reward marked as used", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Error updating reward: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
    }
    
    private void addMockUsers(List<Map<String, Object>> userList) {
        // This is just for demo purposes
        for (int i = userList.size(); i < 5; i++) {
            java.util.Map<String, Object> mockUser = new java.util.HashMap<>();
            mockUser.put("email", "user" + i + "@example.com");
            mockUser.put("fullName", "Test User " + i);
            mockUser.put("role", "user");
            userList.add(mockUser);
        }
    }
    
    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
        
        private List<Map<String, Object>> userList;
        private OnUserClickListener listener;
        
        UserAdapter(List<Map<String, Object>> userList) {
            this.userList = userList;
        }
        
        public void setOnUserClickListener(OnUserClickListener listener) {
            this.listener = listener;
        }
        
        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            Map<String, Object> user = userList.get(position);
            
            // Set user data
            String name = user.containsKey("fullName") ? (String) user.get("fullName") : "Unknown";
            String email = user.containsKey("email") ? (String) user.get("email") : "No email";
            boolean isAdmin = user.containsKey("isAdmin") && (boolean) user.get("isAdmin");
            
            holder.nameText.setText(name);
            holder.emailText.setText(email);
            
            // Set role
            if (isAdmin) {
                holder.roleText.setText("Admin");
                holder.roleText.setChipBackgroundColorResource(R.color.coffee_dark);
            } else {
                holder.roleText.setText("User");
                holder.roleText.setChipBackgroundColorResource(R.color.coffee_accent);
            }

            // You can customize the avatar based on user properties if needed
            // For example, different color for admins
            if (isAdmin) {
                holder.avatarImage.setBackgroundResource(R.drawable.bg_circle_admin);
            } else {
                holder.avatarImage.setBackgroundResource(R.drawable.bg_circle_user);
            }
            
            // Set click listener
            final int adapterPosition = position;
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClick(adapterPosition);
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return userList.size();
        }
        
        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView nameText;
            TextView emailText;
            com.google.android.material.chip.Chip roleText;
            ImageView avatarImage;
            
            UserViewHolder(@NonNull View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.text_user_name);
                emailText = itemView.findViewById(R.id.text_user_email);
                roleText = itemView.findViewById(R.id.text_user_role);
                avatarImage = itemView.findViewById(R.id.image_user_avatar);
            }
        }
    }
    
    // Interface for user click events
    interface OnUserClickListener {
        void onUserClick(int position);
    }
} 