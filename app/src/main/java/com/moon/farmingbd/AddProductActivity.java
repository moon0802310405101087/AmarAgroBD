package com.moon.farmingbd;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddProductActivity extends AppCompatActivity {

    private EditText productNameInput, productPriceInput;
    private Button saveProductButton;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference productsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Initialize the views
        productNameInput = findViewById(R.id.productNameInput);
        productPriceInput = findViewById(R.id.productPriceInput);
        saveProductButton = findViewById(R.id.saveProductButton);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set up Firebase Database reference
        database = FirebaseDatabase.getInstance();
        productsRef = database.getReference("products");

        // Set onClickListener for the Save Product button
        saveProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveProduct();
            }
        });
    }

    private void saveProduct() {
        // Get the product name and price entered by the user
        String productName = productNameInput.getText().toString().trim();
        String productPrice = productPriceInput.getText().toString().trim();

        // Check if the inputs are valid
        if (TextUtils.isEmpty(productName)) {
            Toast.makeText(AddProductActivity.this, "Please enter a product name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(productPrice)) {
            Toast.makeText(AddProductActivity.this, "Please enter a product price", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert price to a double
        double price;
        try {
            price = Double.parseDouble(productPrice);
        } catch (NumberFormatException e) {
            Toast.makeText(AddProductActivity.this, "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the current user UID from Firebase Authentication
        String userId = mAuth.getCurrentUser().getUid();

        // Create a new Product object
        Product newProduct = new Product(productName, price);

        // Generate a new product ID using push() and save under the specific user's ID
        String productId = productsRef.child(userId).push().getKey();

        if (productId != null) {
            Log.d("Firebase", "Saving product for user ID: " + userId + " with product ID: " + productId);
            // Save the product to Firebase under the user-specific path
            productsRef.child(userId).child(productId).setValue(newProduct).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("Firebase", "Product saved successfully!");
                    Toast.makeText(AddProductActivity.this, "Product saved successfully!", Toast.LENGTH_SHORT).show();
                    // Clear inputs after saving
                    productNameInput.setText("");
                    productPriceInput.setText("");
                } else {
                    Log.e("Firebase", "Failed to save product", task.getException());
                    Toast.makeText(AddProductActivity.this, "Failed to save product", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e("Firebase", "Failed to generate product ID");
        }
    }

    // Product model class to store product data
    public static class Product {
        public String name;
        public double price;

        // Constructor for Product class
        public Product(String name, double price) {
            this.name = name;
            this.price = price;
        }
    }
}
