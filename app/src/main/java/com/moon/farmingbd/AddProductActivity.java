package com.moon.farmingbd;


import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddProductActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText productNameInput, productPriceInput, productQuantityInput, productDescriptionInput;
    private Spinner priceUnitSpinner, quantityUnitSpinner, categorySpinner, locationSpinner;
    private Button saveProductButton, chooseImageButton;
    private ImageView productImageView;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference productsRef;
    private Uri productImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        productNameInput = findViewById(R.id.productNameInput);
        productPriceInput = findViewById(R.id.productPriceInput);
        productQuantityInput = findViewById(R.id.productQuantityInput);
        productDescriptionInput = findViewById(R.id.productDescriptionInput);
        priceUnitSpinner = findViewById(R.id.priceUnitSpinner);
        quantityUnitSpinner = findViewById(R.id.quantityUnitSpinner);
        categorySpinner = findViewById(R.id.categorySpinner);
        locationSpinner = findViewById(R.id.locationSpinner);
        saveProductButton = findViewById(R.id.saveProductButton);
        chooseImageButton = findViewById(R.id.chooseImageButton);
        productImageView = findViewById(R.id.productImageView);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        productsRef = database.getReference("products");

        chooseImageButton.setOnClickListener(v -> openFileChooser());

        saveProductButton.setOnClickListener(v -> saveProduct());
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            productImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), productImageUri);
                productImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveProduct() {
        String productName = productNameInput.getText().toString().trim();
        String productPriceStr = productPriceInput.getText().toString().trim();
        String productQuantityStr = productQuantityInput.getText().toString().trim();
        String productDescription = productDescriptionInput.getText().toString().trim();
        String priceUnit = priceUnitSpinner.getSelectedItem().toString();
        String quantityUnit = quantityUnitSpinner.getSelectedItem().toString();
        String category = categorySpinner.getSelectedItem().toString();
        String location = locationSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(productName) || TextUtils.isEmpty(productPriceStr) || TextUtils.isEmpty(productQuantityStr) || TextUtils.isEmpty(productDescription)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double price, quantity;
        try {
            price = Double.parseDouble(productPriceStr);
            quantity = Double.parseDouble(productQuantityStr);
            if (price <= 0 || quantity <= 0) {
                Toast.makeText(this, "Price and quantity must be positive", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            return;
        }

        String imageBase64 = null;
        if (productImageUri != null) {
            imageBase64 = convertImageToBase64(productImageUri);
        }

        String userId = mAuth.getCurrentUser().getUid();
        String productId = productsRef.child(userId).push().getKey();

        if (productId != null) {
            Product product = new Product(productName, price, quantity, productDescription, priceUnit, quantityUnit, category, location, imageBase64);
            productsRef.child(userId).child(productId).setValue(product).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Product saved successfully!", Toast.LENGTH_SHORT).show();
                    clearInputs();
                } else {
                    Toast.makeText(this, "Failed to save product", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String convertImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.encodeToString(imageBytes, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void clearInputs() {
        productNameInput.setText("");
        productPriceInput.setText("");
        productQuantityInput.setText("");
        productDescriptionInput.setText("");
        productImageView.setImageResource(R.drawable.ic_placeholder);
    }

    public static class Product {
        public String name;
        public double price;
        public double quantity;
        public String description;
        public String priceUnit;
        public String quantityUnit;
        public String category;
        public String location;
        public String imageBase64;

        public Product() {}

        public Product(String name, double price, double quantity, String description, String priceUnit, String quantityUnit, String category, String location, String imageBase64) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.description = description;
            this.priceUnit = priceUnit;
            this.quantityUnit = quantityUnit;
            this.category = category;
            this.location = location;
            this.imageBase64 = imageBase64;
        }
    }
}
