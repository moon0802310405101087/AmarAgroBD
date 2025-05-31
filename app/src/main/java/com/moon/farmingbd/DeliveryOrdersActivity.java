package com.moon.farmingbd;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class DeliveryOrdersActivity extends AppCompatActivity {

    private ListView deliveryOrdersListView;
    private OrderAdapter orderAdapter;
    private List<Order> assignedOrders;

    private FirebaseAuth auth;
    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_orders);

        deliveryOrdersListView = findViewById(R.id.deliveryOrdersListView);
        assignedOrders = new ArrayList<>();

        auth = FirebaseAuth.getInstance();
        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        orderAdapter = new OrderAdapter(this, assignedOrders, "delivery");
        deliveryOrdersListView.setAdapter(orderAdapter);

        loadAssignedOrders();
    }

    private void loadAssignedOrders() {
        String deliveryEmail = auth.getCurrentUser().getEmail();

        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                assignedOrders.clear();
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Order order = orderSnap.getValue(Order.class);
                    if (order != null && deliveryEmail.equals(order.getDeliveryManEmail())) {
                        order.setId(orderSnap.getKey()); // set Firebase push
                        assignedOrders.add(order);
                    }
                }
                orderAdapter.notifyDataSetChanged();

                if (assignedOrders.isEmpty()) {
                    Toast.makeText(DeliveryOrdersActivity.this, "No assigned orders.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(DeliveryOrdersActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
