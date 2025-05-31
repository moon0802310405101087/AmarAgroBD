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
    private ArrayList<String> productList;
    private ArrayAdapter<String> adapter;


    private FirebaseDatabase database;
    private DatabaseReference productsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_products);

        productListView = findViewById(R.id.productListView);
        productList = new ArrayList<>();


        database = FirebaseDatabase.getInstance();
        productsRef = database.getReference("products");


        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, productList);
        productListView.setAdapter(adapter);


        fetchProductsFromFirebase();


        productListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedProduct = productList.get(position);

            Toast.makeText(OrderProductsActivity.this, "Clicked: " + selectedProduct, Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchProductsFromFirebase() {
        productsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productList.clear();


                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    String product = productSnapshot.getValue(String.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(OrderProductsActivity.this, "Failed to load products.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
