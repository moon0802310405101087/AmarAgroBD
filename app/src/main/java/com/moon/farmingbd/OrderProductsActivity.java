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

public class OrderProductsActivity extends AppCompatActivity {

    private ListView productListView;
    private ArrayList<String> productList; // Holds the product names
    private ArrayAdapter<String> adapter;

    // Firebase reference
    private FirebaseDatabase database;
    private DatabaseReference productsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_products); // Set the layout

        productListView = findViewById(R.id.productListView); // Initialize ListView
        productList = new ArrayList<>(); // Initialize ArrayList

        // Set up Firebase database reference
        database = FirebaseDatabase.getInstance();
        productsRef = database.getReference("products"); // Adjust based on your Firebase structure

        // Set up ArrayAdapter to bind data to ListView
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productList);
        productListView.setAdapter(adapter); // Attach adapter to ListView

        // Fetch products from Firebase
        fetchProductsFromFirebase();

        // Handle item click events
        productListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedProduct = productList.get(position);
            // Handle the click event, for example, order the product
            Toast.makeText(OrderProductsActivity.this, "Clicked: " + selectedProduct, Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchProductsFromFirebase() {
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productList.clear(); // Clear existing data

                // Loop through the Firebase snapshot to fetch product data
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    String product = productSnapshot.getValue(String.class); // Get the product name
                    if (product != null) {
                        productList.add(product); // Add product to list
                    }
                }

                // Notify the adapter to update the ListView
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(OrderProductsActivity.this, "Failed to load products.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
