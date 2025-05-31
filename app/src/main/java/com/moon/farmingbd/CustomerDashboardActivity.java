package com.moon.farmingbd;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class CustomerDashboardActivity extends AppCompatActivity {

    private TextView orderCountTextView, totalPriceTextView,
            deliveredCountTextView, pendingCountTextView, underWayCountTextView;

    // New UI elements for profile info
    private TextView profileNameTextView, profileEmailTextView;
    private ImageView profileImageView;

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;
    private DatabaseReference ordersRef, userRef;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        mAuth = FirebaseAuth.getInstance();

        orderCountTextView = findViewById(R.id.orderCountTextView);
        totalPriceTextView = findViewById(R.id.totalPriceTextView);
        deliveredCountTextView = findViewById(R.id.deliveredCountTextView);
        pendingCountTextView = findViewById(R.id.pendingCountTextView);
        underWayCountTextView = findViewById(R.id.underWayCountTextView);


        profileNameTextView = findViewById(R.id.profileNameTextView);
        profileEmailTextView = findViewById(R.id.profileEmailTextView);
        profileImageView = findViewById(R.id.profileImageView);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        bottomNavigationView.setSelectedItemId(R.id.nav_customer_dashboard);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_customer_dashboard) {
                return true;
            } else if (id == R.id.nav_view_products) {
                startActivity(new Intent(CustomerDashboardActivity.this, ViewProductsActivity.class));
                return true;
            } else if (id == R.id.nav_order_history) {
                startActivity(new Intent(CustomerDashboardActivity.this, OrderHistoryActivity.class));
                return true;
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(CustomerDashboardActivity.this, MainActivity.class));
                finish();
                return true;
            }
            return false;
        });


        orderCountTextView.setOnClickListener(v -> {
            startActivity(new Intent(CustomerDashboardActivity.this, OrderHistoryActivity.class));
        });


        deliveredCountTextView.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String currentUserId = currentUser.getUid();
                Intent intent = new Intent(CustomerDashboardActivity.this, ConfirmedOrdersActivity.class);
                intent.putExtra("customerId", currentUserId);
                startActivity(intent);
            }
        });


        pendingCountTextView.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String currentUserId = currentUser.getUid();
                Intent intent = new Intent(CustomerDashboardActivity.this, PendingOrdersActivity.class);
                intent.putExtra("customerId", currentUserId);
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(this::refreshDashboardData);


        loadUserProfile();

        refreshDashboardData();
    }

    private void refreshDashboardData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            ordersRef = FirebaseDatabase.getInstance().getReference("orders");

            ordersRef.orderByChild("customerId").equalTo(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            long orderCount = snapshot.getChildrenCount();
                            double totalSpent = 0.0;
                            int deliveredCount = 0;
                            int pendingCount = 0;
                            int underWayCount = 0;

                            for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                                Order order = orderSnapshot.getValue(Order.class);
                                if (order != null) {
                                    String status = order.getOrderStatus();
                                    String deliveryManEmail = order.getDeliveryManEmail();

                                    if ("Delivered".equalsIgnoreCase(status)) {
                                        totalSpent += order.getTotalPrice();
                                        deliveredCount++;
                                    } else if (!"Cancelled".equalsIgnoreCase(status)) {
                                        pendingCount++;

                                        if (deliveryManEmail != null && !deliveryManEmail.isEmpty()) {
                                            underWayCount++;
                                        }
                                    }
                                }
                            }

                            orderCountTextView.setText("Total Orders\n" + orderCount);
                            totalPriceTextView.setText(String.format("Total Spent TK\n%.2f", totalSpent));
                            deliveredCountTextView.setText("Delivered\n" + deliveredCount);
                            pendingCountTextView.setText("Pending\n" + pendingCount);
                            underWayCountTextView.setText("On Going\n" + underWayCount + " Product");

                            swipeRefreshLayout.setRefreshing(false);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(CustomerDashboardActivity.this,
                                    "Failed to load order data", Toast.LENGTH_SHORT).show();
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    });

        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String uid = currentUser.getUid();
        String email = currentUser.getEmail();
        profileEmailTextView.setText(email);

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String profileImageBase64 = snapshot.child("profileImage").getValue(String.class);

                    if (name != null) {
                        profileNameTextView.setText(name);
                    }

                    if (profileImageBase64 != null && !profileImageBase64.isEmpty()) {
                        try {
                            byte[] decodedBytes = Base64.decode(profileImageBase64, Base64.DEFAULT);
                            Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            profileImageView.setImageBitmap(decodedBitmap);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CustomerDashboardActivity.this, "Failed to load profile info", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton openProfileButton = findViewById(R.id.openProfileButton);
        openProfileButton.setOnClickListener(v -> {
            startActivity(new Intent(CustomerDashboardActivity.this, ProfileActivity.class));
        });
    }

}
