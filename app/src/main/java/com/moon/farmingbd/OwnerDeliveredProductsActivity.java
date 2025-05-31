package com.moon.farmingbd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import com.google.android.material.bottomnavigation.BottomNavigationView;
public class OwnerDeliveredProductsActivity extends AppCompatActivity {

    private RecyclerView deliveredOrdersRecyclerView;
    private TextView summaryTextView;
    private Spinner filterSpinner;
    private SwipeRefreshLayout swipeRefreshLayout;

    private DeliveredOrdersAdapter deliveredOrdersAdapter;
    private ArrayList<Order> deliveredOrdersList = new ArrayList<>();

    private DatabaseReference ordersRef;
    private String ownerId;
    private String currentFilter = "Monthly";  // Default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_delivered_products);

        ownerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        deliveredOrdersRecyclerView = findViewById(R.id.deliveredOrdersRecyclerView);
        summaryTextView = findViewById(R.id.summaryTextView);
        filterSpinner = findViewById(R.id.filterSpinner);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        deliveredOrdersRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        deliveredOrdersAdapter = new DeliveredOrdersAdapter(deliveredOrdersList);
        deliveredOrdersRecyclerView.setAdapter(deliveredOrdersAdapter);

        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        setupFilterSpinner();
        setupSwipeRefresh();
        loadDeliveredOrders(currentFilter);
        setupBottomNavigation();
    }

    private void setupFilterSpinner() {
        String[] filters = {"Weekly", "Monthly", "Yearly"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);
        filterSpinner.setSelection(1); // Monthly

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = (String) parent.getItemAtPosition(position);
                loadDeliveredOrders(currentFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentFilter = "Monthly";
                loadDeliveredOrders(currentFilter);
            }
        });
    }
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_reports); // Highlight current

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(this, DashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_show_products) {
                startActivity(new Intent(this, ShowProductsActivity.class));
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(this, ViewOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_reports) {
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
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            deliveredOrdersList.clear();
            loadDeliveredOrders(currentFilter);
        });
    }

    private void loadDeliveredOrders(String filter) {
        ordersRef.orderByChild("ownerId").equalTo(ownerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                deliveredOrdersList.clear();

                double totalEarnings = 0;
                int totalDeliveries = 0;
                long now = System.currentTimeMillis();
                Calendar calendarNow = Calendar.getInstance();
                calendarNow.setTimeInMillis(now);

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);

                    if (order != null && "Delivered".equalsIgnoreCase(order.getOrderStatus())) {
                        long deliveryTime = order.getDeliveryTimestamp();
                        if (deliveryTime <= 0) continue;

                        if (isWithinFilter(deliveryTime, calendarNow, filter)) {
                            deliveredOrdersList.add(order);
                            totalEarnings += order.getTotalPrice();
                            totalDeliveries++;
                        }
                    }
                }

                Collections.sort(deliveredOrdersList, (o1, o2) ->
                        Long.compare(o2.getDeliveryTimestamp(), o1.getDeliveryTimestamp()));

                deliveredOrdersAdapter.notifyDataSetChanged();

                String summaryText = String.format(Locale.getDefault(),
                        "Total Earnings: ৳%.2f | Deliveries: %d",
                        totalEarnings, totalDeliveries);
                summaryTextView.setText(summaryText);

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OwnerDeliveredProductsActivity.this, "Failed to load delivered orders.", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private boolean isWithinFilter(long deliveryTime, Calendar now, String filter) {
        Calendar deliveryCal = Calendar.getInstance();
        deliveryCal.setTimeInMillis(deliveryTime);

        switch (filter.toLowerCase()) {
            case "weekly":
                return deliveryCal.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)
                        && deliveryCal.get(Calendar.YEAR) == now.get(Calendar.YEAR);
            case "monthly":
                return deliveryCal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                        && deliveryCal.get(Calendar.YEAR) == now.get(Calendar.YEAR);
            case "yearly":
                return deliveryCal.get(Calendar.YEAR) == now.get(Calendar.YEAR);
            default:
                return false;
        }
    }
}
