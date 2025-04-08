package com.example.pointbrewproject.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
                        usersRecyclerView.setAdapter(new UserAdapter(userList));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error loading users: " + e.getMessage(), 
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
        
        UserAdapter(List<Map<String, Object>> userList) {
            this.userList = userList;
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
            
            holder.nameText.setText((String) user.get("fullName"));
            holder.emailText.setText((String) user.get("email"));
            
            // Set role text
            String role = (String) user.get("role");
            if ("admin".equals(role)) {
                holder.roleText.setText("Admin");
                holder.roleText.setTextColor(getResources().getColor(R.color.primary_blue));
            } else {
                holder.roleText.setText("User");
                holder.roleText.setTextColor(getResources().getColor(R.color.dark_gray));
            }
        }
        
        @Override
        public int getItemCount() {
            return userList.size();
        }
        
        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView nameText;
            TextView emailText;
            TextView roleText;
            
            UserViewHolder(@NonNull View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.text_user_name);
                emailText = itemView.findViewById(R.id.text_user_email);
                roleText = itemView.findViewById(R.id.text_user_role);
            }
        }
    }
} 