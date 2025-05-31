package com.moon.farmingbd;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PendingOrdersActivity extends AppCompatActivity {

    private ListView pendingOrdersListView;
    private OrderHistoryAdapter adapter;
    private List<Order> pendingOrdersList;

    private String customerId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_orders);

        pendingOrdersListView = findViewById(R.id.pendingOrdersListView);
        pendingOrdersList = new ArrayList<>();
        adapter = new OrderHistoryAdapter(this, pendingOrdersList);
        pendingOrdersListView.setAdapter(adapter);

        customerId = getIntent().getStringExtra("customerId");

        if (customerId == null || customerId.isEmpty()) {
            Toast.makeText(this, "No customer ID provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadPendingOrdersForCustomer(customerId);
    }

    private void loadPendingOrdersForCustomer(String customerId) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                pendingOrdersList.clear();

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);

                    if (order != null
                            && !"Delivered".equalsIgnoreCase(order.getOrderStatus())
                            && !"Cancelled".equalsIgnoreCase(order.getOrderStatus())
                            && customerId.equals(order.getCustomerId())) {

                        order.setId(orderSnapshot.getKey());
                        pendingOrdersList.add(order);
                    }
                }

                Collections.reverse(pendingOrdersList);

                if (pendingOrdersList.isEmpty()) {
                    Toast.makeText(PendingOrdersActivity.this, "No pending orders found for this customer.", Toast.LENGTH_SHORT).show();
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(PendingOrdersActivity.this, "Failed to load orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
