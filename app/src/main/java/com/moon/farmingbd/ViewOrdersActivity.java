package com.moon.farmingbd;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ViewOrdersActivity extends AppCompatActivity {

    private ListView ordersListView;
    private ArrayList<Order> ordersList; // Holds the order data
    private ArrayAdapter<Order> adapter;

    // Firebase reference
    private FirebaseDatabase database;
    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_orders); // Set the layout

        ordersListView = findViewById(R.id.ordersListView); // Initialize ListView
        ordersList = new ArrayList<>(); // Initialize ArrayList

        // Set up Firebase database reference
        database = FirebaseDatabase.getInstance();
        ordersRef = database.getReference("orders"); // Adjust based on your Firebase structure

        // Set up ArrayAdapter to bind data to ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ordersList);
        ordersListView.setAdapter(adapter); // Attach adapter to ListView

        // Fetch orders from Firebase
        fetchOrdersFromFirebase();

        // Handle item click events
        ordersListView.setOnItemClickListener((parent, view, position, id) -> {
            Order selectedOrder = ordersList.get(position);
            // Handle the click event, for example, show order details
            Toast.makeText(ViewOrdersActivity.this, "Clicked: " + selectedOrder.toString(), Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchOrdersFromFirebase() {
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ordersList.clear(); // Clear existing data

                // Loop through the Firebase snapshot to fetch order data
                for (DataSnapshot orderSnapshot : dataSnapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class); // Get the order details
                    if (order != null) {
                        ordersList.add(order); // Add order to list
                    }
                }

                // Notify the adapter to update the ListView
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(ViewOrdersActivity.this, "Failed to load orders.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
