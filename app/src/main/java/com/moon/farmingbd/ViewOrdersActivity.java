package com.moon.farmingbd;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ViewOrdersActivity extends AppCompatActivity {

    private ListView ordersListView;
    private ArrayList<Order> ordersList;
    private OrderAdapter adapter;

    private DatabaseReference ordersRef;
    private String currentOwnerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_orders);

        ordersListView = findViewById(R.id.ordersListView);
        ordersList = new ArrayList<>();

        ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        currentOwnerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        adapter = new OrderAdapter(this, ordersList, "owner");
        ordersListView.setAdapter(adapter);

        fetchOrdersFromFirebase();

        setupBottomNavigation();
    }

    private void fetchOrdersFromFirebase() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ordersList.clear();
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Order order = orderSnap.getValue(Order.class);
                    if (order != null && currentOwnerId.equals(order.getOwnerId())) {
                        order.setId(orderSnap.getKey()); // Add Firebase push ID
                        ordersList.add(order);
                    }
                }


                Collections.sort(ordersList, new Comparator<Order>() {
                    @Override
                    public int compare(Order o1, Order o2) {
                        return Long.compare(o2.getTimestamp(), o1.getTimestamp());
                    }
                });

                if (ordersList.isEmpty()) {
                    Toast.makeText(ViewOrdersActivity.this, "No orders found.", Toast.LENGTH_SHORT).show();
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ViewOrdersActivity.this, "Failed to load orders.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_orders);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_show_products) {
                startActivity(new Intent(this, ShowProductsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_orders) {

                return true;
            } else if (itemId == R.id.nav_reports) {
                startActivity(new Intent(this, SalesReportsActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }
}
