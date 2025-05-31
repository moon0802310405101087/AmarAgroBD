package com.moon.farmingbd;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private ListView orderListView;
    private List<Order> orderList;
    private OrderHistoryAdapter adapter;
    private DatabaseReference ordersRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        orderListView = findViewById(R.id.orderListView);

        orderList = new ArrayList<>();
        adapter = new OrderHistoryAdapter(this, orderList);
        orderListView.setAdapter(adapter);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ordersRef = FirebaseDatabase.getInstance().getReference("orders");

        ordersRef.orderByChild("customerId").equalTo(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderList.clear();

                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                            Order order = orderSnapshot.getValue(Order.class);

                            if (order != null) {
                                order.setId(orderSnapshot.getKey());
                                orderList.add(order);
                            }
                        }

                        adapter.notifyDataSetChanged();

                        if(orderList.isEmpty()) {
                            Toast.makeText(OrderHistoryActivity.this, "No orders found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(OrderHistoryActivity.this, "Failed to load orders: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
