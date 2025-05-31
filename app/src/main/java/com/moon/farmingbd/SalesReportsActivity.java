package com.moon.farmingbd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

public class SalesReportsActivity extends AppCompatActivity {

    private RecyclerView availableProductsRecyclerView, deliveredOrdersRecyclerView;
    private TextView noProductsTextView, summaryTextView;
    private Spinner filterSpinner;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Button buttonShowAll;

    private AvailableProductsAdapter availableProductsAdapter;
    private DeliveredOrdersAdapter deliveredOrdersAdapter;

    private ArrayList<Product> availableProductsList = new ArrayList<>();
    private ArrayList<Order> deliveredOrdersList = new ArrayList<>();

    private DatabaseReference productsRef, ordersRef;
    private String ownerId;

    private String currentFilter = "Monthly";


    private int loadingCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_reports);

        ownerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        availableProductsRecyclerView = findViewById(R.id.availableProductsRecyclerView);
        deliveredOrdersRecyclerView = findViewById(R.id.deliveredOrdersRecyclerView);
        noProductsTextView = findViewById(R.id.noProductsTextView);
        summaryTextView = findViewById(R.id.summaryTextView);
        filterSpinner = findViewById(R.id.filterSpinner);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        buttonShowAll = findViewById(R.id.showAllButton);

        availableProductsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        deliveredOrdersRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        availableProductsAdapter = new AvailableProductsAdapter(availableProductsList);
        deliveredOrdersAdapter = new DeliveredOrdersAdapter(deliveredOrdersList);

        availableProductsRecyclerView.setAdapter(availableProductsAdapter);
        deliveredOrdersRecyclerView.setAdapter(deliveredOrdersAdapter);

        // Firebase refs
        productsRef = FirebaseDatabase.getInstance().getReference("products").child(ownerId);
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        setupFilterSpinner();
        setupSwipeToRefresh();
        setupBottomNavigation();

        buttonShowAll.setOnClickListener(v -> {
            Intent intent = new Intent(SalesReportsActivity.this, OwnerDeliveredProductsActivity.class);
            startActivity(intent);
        });

        loadAvailableProducts();
        loadDeliveredOrders(currentFilter);
    }

    private void setupFilterSpinner() {
        String[] filters = {"Weekly", "Monthly", "Yearly"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);

        filterSpinner.setSelection(1);

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

    private void setupSwipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {

            availableProductsList.clear();
            deliveredOrdersList.clear();

            loadAvailableProducts();
            loadDeliveredOrders(currentFilter);
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

    private void loadAvailableProducts() {
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                availableProductsList.clear();

                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        availableProductsList.add(product);
                    }
                }

                noProductsTextView.setVisibility(availableProductsList.isEmpty() ? View.VISIBLE : View.GONE);
                availableProductsAdapter.notifyDataSetChanged();

                stopRefreshingIfNeeded();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SalesReportsActivity.this, "Failed to load products.", Toast.LENGTH_SHORT).show();
                stopRefreshingIfNeeded();
            }
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


                Collections.sort(deliveredOrdersList, (o1, o2) -> Long.compare(o2.getDeliveryTimestamp(), o1.getDeliveryTimestamp()));

                deliveredOrdersAdapter.notifyDataSetChanged();

                String summaryText = String.format(Locale.getDefault(),
                        "Total Earnings: ৳%.2f | Deliveries: %d",
                        totalEarnings, totalDeliveries);
                summaryTextView.setText(summaryText);

                stopRefreshingIfNeeded();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SalesReportsActivity.this, "Failed to load orders.", Toast.LENGTH_SHORT).show();
                stopRefreshingIfNeeded();
            }
        });
    }

    private void stopRefreshingIfNeeded() {
        loadingCounter++;
        if (loadingCounter >= 2) {
            swipeRefreshLayout.setRefreshing(false);
            loadingCounter = 0;
        }
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
