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

public class ConfirmedOrdersActivity extends AppCompatActivity {

    private ListView confirmedOrdersListView;
    private OrderHistoryAdapter adapter;
    private List<Order> confirmedOrdersList;

    private String customerId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmed_orders);

        confirmedOrdersListView = findViewById(R.id.confirmedOrdersListView);
        confirmedOrdersList = new ArrayList<>();
        adapter = new OrderHistoryAdapter(this, confirmedOrdersList);
        confirmedOrdersListView.setAdapter(adapter);

        customerId = getIntent().getStringExtra("customerId");

        if (customerId == null || customerId.isEmpty()) {
            Toast.makeText(this, "No customer ID provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadConfirmedOrdersForCustomer(customerId);
    }

    private void loadConfirmedOrdersForCustomer(String customerId) {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                confirmedOrdersList.clear();

                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);

                    if (order != null
                            && "Delivered".equalsIgnoreCase(order.getOrderStatus())
                            && customerId.equals(order.getCustomerId())) {

                        order.setId(orderSnapshot.getKey());
                        confirmedOrdersList.add(order);
                    }
                }

                Collections.reverse(confirmedOrdersList);

                if (confirmedOrdersList.isEmpty()) {
                    Toast.makeText(ConfirmedOrdersActivity.this, "No confirmed orders found for this customer.", Toast.LENGTH_SHORT).show();
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ConfirmedOrdersActivity.this, "Failed to load orders: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
