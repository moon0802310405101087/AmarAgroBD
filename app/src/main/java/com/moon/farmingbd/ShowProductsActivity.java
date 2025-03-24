package com.moon.farmingbd;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShowProductsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference productsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_products);

        // Initialize Firebase instances
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        productsRef = database.getReference("products");

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        // Fetch and display products for the current user
        fetchProducts();
    }

    private void fetchProducts() {
        // Get the current user UID from Firebase Authentication
        String userId = mAuth.getCurrentUser().getUid();

        // Retrieve products for the specific user from Firebase
        productsRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear the existing product list
                productList.clear();

                // Loop through the products and add them to the list
                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        productList.add(product);
                    }
                }

                // Notify the adapter that data has been updated
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("Firebase", "Failed to load products", databaseError.toException());
                Toast.makeText(ShowProductsActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Adapter for the RecyclerView
    public static class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

        private List<Product> productList;

        public ProductAdapter(List<Product> productList) {
            this.productList = productList;
        }

        @Override
        public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // Inflate the item layout
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ProductViewHolder holder, int position) {
            Product product = productList.get(position);
            holder.productNameTextView.setText(product.name);
            holder.productPriceTextView.setText(String.format("Price Taka: %.2f", product.price));
        }

        @Override
        public int getItemCount() {
            return productList.size();
        }

        // ViewHolder for individual product items
        public static class ProductViewHolder extends RecyclerView.ViewHolder {

            TextView productNameTextView;
            TextView productPriceTextView;

            public ProductViewHolder(View itemView) {
                super(itemView);
                productNameTextView = itemView.findViewById(R.id.productNameTextView);
                productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
            }
        }
    }

    // Product model class to store product data
    public static class Product {
        public String name;
        public double price;

        // Default constructor for Firebase
        public Product() {
            // Default constructor required for calls to DataSnapshot.getValue(Product.class)
        }

        // Constructor for Product class
        public Product(String name, double price) {
            this.name = name;
            this.price = price;
        }
    }
}
