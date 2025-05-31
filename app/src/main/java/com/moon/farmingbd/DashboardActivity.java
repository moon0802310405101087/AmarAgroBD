package com.moon.farmingbd;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private TextView totalProductsTextView, totalEarningsTextView, totalDeliveredTextView;
    private TextView newOrdersCountTextView;

    private Button btnViewProfile;

    private RecyclerView recyclerNewOrders;
    private NewOrdersAdapter newOrdersAdapter;
    private ArrayList<Order> newOrdersList = new ArrayList<>();

    private DatabaseReference productsRef, ordersRef;
    private FirebaseAuth mAuth;

    private SwipeRefreshLayout swipeRefreshLayout;

    // Profile
    private ImageView profileImageView;
    private TextView profileNameTextView, profileEmailTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize
        profileImageView = findViewById(R.id.profileImageView);
        profileNameTextView = findViewById(R.id.profileNameTextView);
        profileEmailTextView = findViewById(R.id.profileEmailTextView);

        // Initialize
        totalProductsTextView = findViewById(R.id.totalProductsTextView);
        totalEarningsTextView = findViewById(R.id.totalEarningsTextView);
        totalDeliveredTextView = findViewById(R.id.totalDeliveredTextView);
        newOrdersCountTextView = findViewById(R.id.newOrdersCountTextView);

        btnViewProfile = findViewById(R.id.openProfileButton);

        recyclerNewOrders = findViewById(R.id.recyclerNewOrders);
        recyclerNewOrders.setLayoutManager(new LinearLayoutManager(this));
        newOrdersAdapter = new NewOrdersAdapter(newOrdersList);
        recyclerNewOrders.setAdapter(newOrdersAdapter);
        recyclerNewOrders.setVisibility(View.GONE);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        productsRef = FirebaseDatabase.getInstance().getReference("products").child(userId);
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        loadUserProfile();

        loadAllData(userId);


        swipeRefreshLayout.setOnRefreshListener(() -> loadAllData(userId));


        btnViewProfile.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        totalProductsTextView.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ShowProductsActivity.class);
            startActivity(intent);
        });


        totalDeliveredTextView.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, OwnerDeliveredProductsActivity.class);
            startActivity(intent);
        });


        newOrdersCountTextView.setOnClickListener(v -> {
            if (newOrdersList.isEmpty()) {
                Toast.makeText(DashboardActivity.this, "No new orders at the moment", Toast.LENGTH_SHORT).show();
                recyclerNewOrders.setVisibility(View.GONE);
            } else {
                if (recyclerNewOrders.getVisibility() == View.VISIBLE) {
                    recyclerNewOrders.setVisibility(View.GONE);
                } else {
                    recyclerNewOrders.setVisibility(View.VISIBLE);
                }
            }
        });


        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_add_product) {
                startActivity(new Intent(DashboardActivity.this, AddProductActivity.class));
                return true;

            } else if (itemId == R.id.nav_show_products) {
                startActivity(new Intent(DashboardActivity.this, ShowProductsActivity.class));
                return true;

            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(DashboardActivity.this, ViewOrdersActivity.class));
                return true;

            } else if (itemId == R.id.nav_reports) {
                startActivity(new Intent(DashboardActivity.this, SalesReportsActivity.class));
                return true;

            } else if (itemId == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(DashboardActivity.this, MainActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    private void loadUserProfile() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        profileEmailTextView.setText(email != null ? email : "N/A");

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String base64Image = snapshot.child("profileImage").getValue(String.class);

                    profileNameTextView.setText(name != null ? name : "N/A");

                    if (base64Image != null && !base64Image.isEmpty()) {
                        try {
                            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                            profileImageView.setImageBitmap(bitmap);
                        } catch (IllegalArgumentException e) {
                            profileImageView.setImageResource(R.drawable.ic_placeholder);
                        }
                    } else {
                        profileImageView.setImageResource(R.drawable.ic_placeholder);
                    }
                } else {
                    profileNameTextView.setText("N/A");
                    profileImageView.setImageResource(R.drawable.ic_placeholder);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllData(String userId) {
        loadProductStats(userId);
        loadOrderStats(userId);
        loadNewOrdersCount(userId);
    }

    private void loadProductStats(String userId) {
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalProducts = 0;
                for (DataSnapshot productSnap : snapshot.getChildren()) {
                    totalProducts++;
                }
                totalProductsTextView.setText("Total Products: " + totalProducts);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                totalProductsTextView.setText("Total Products: Error");
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void loadOrderStats(String userId) {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalDelivered = 0;
                double totalEarnings = 0.0;

                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Order order = orderSnap.getValue(Order.class);
                    if (order != null && userId.equals(order.getOwnerId())) {
                        if ("Delivered".equalsIgnoreCase(order.getOrderStatus())) {
                            totalDelivered++;
                            totalEarnings += order.getTotalPrice();
                        }
                    }
                }

                totalDeliveredTextView.setText("Total Delivered Orders: " + totalDelivered);
                totalEarningsTextView.setText("Total Earnings: " + totalEarnings + " BDT");
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                totalDeliveredTextView.setText("Delivered Orders: Error");
                totalEarningsTextView.setText("Earnings: Error");
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void loadNewOrdersCount(String userId) {
        ordersRef.orderByChild("ownerId").equalTo(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int newOrdersCount = 0;
                newOrdersList.clear();

                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Order order = orderSnap.getValue(Order.class);
                    if (order != null && "Pending".equalsIgnoreCase(order.getOrderStatus())) {
                        newOrdersCount++;
                        newOrdersList.add(order);
                    }
                }

                if (newOrdersCount > 0) {
                    newOrdersCountTextView.setText("New Orders: " + newOrdersCount);
                    newOrdersCountTextView.setVisibility(View.VISIBLE);
                } else {
                    newOrdersCountTextView.setVisibility(View.GONE);
                    recyclerNewOrders.setVisibility(View.GONE);
                }

                newOrdersAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DashboardActivity.this, "Failed to load new orders", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
