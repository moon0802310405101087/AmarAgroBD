package com.moon.farmingbd;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ShowProductsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    private FirebaseAuth mAuth;
    private DatabaseReference productsRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_products);

        mAuth = FirebaseAuth.getInstance();
        productsRef = FirebaseDatabase.getInstance().getReference("products");
        userId = mAuth.getCurrentUser().getUid();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        fetchProducts();
        setupBottomNavigation();
    }

    private void fetchProducts() {
        productsRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        product.id = productSnapshot.getKey(); // save product key
                        productList.add(product);
                    }
                }

                java.util.Collections.reverse(productList);
                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ShowProductsActivity.this, "Error loading products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

        private List<Product> productList;

        public ProductAdapter(List<Product> productList) {
            this.productList = productList;
        }

        @Override
        public ProductViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ProductViewHolder holder, int position) {
            Product product = productList.get(position);

            holder.productNameTextView.setText("Name: " + product.name);
            holder.productPriceTextView.setText("Price: " + product.price + " Taka/" + product.priceUnit);
            holder.productQuantityTextView.setText("Available: " + product.quantity + " " + product.quantityUnit);
            holder.productDescriptionTextView.setText("Description: " + product.description);
            holder.productCategoryTextView.setText("Category: " + product.category);
            holder.productLocationTextView.setText("Location: " + product.location);

            if (product.imageBase64 != null && !product.imageBase64.isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(product.imageBase64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    holder.productImageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    holder.productImageView.setImageResource(R.drawable.ic_placeholder);
                }
            } else {
                holder.productImageView.setImageResource(R.drawable.ic_placeholder);
            }

            holder.editIcon.setOnClickListener(view -> showEditDialog(product));
            holder.deleteIcon.setOnClickListener(view -> showDeleteConfirmation(product));


            holder.itemView.setOnClickListener(view -> showProductDetailsDialog(product));
        }

        @Override
        public int getItemCount() {
            return productList.size();
        }

        public class ProductViewHolder extends RecyclerView.ViewHolder {
            TextView productNameTextView, productPriceTextView, productQuantityTextView,
                    productDescriptionTextView, productCategoryTextView, productLocationTextView;
            ImageView productImageView, editIcon, deleteIcon;

            public ProductViewHolder(android.view.View itemView) {
                super(itemView);
                productNameTextView = itemView.findViewById(R.id.productNameTextView);
                productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
                productQuantityTextView = itemView.findViewById(R.id.productQuantityTextView);
                productDescriptionTextView = itemView.findViewById(R.id.productDescriptionTextView);
                productCategoryTextView = itemView.findViewById(R.id.productCategoryTextView);
                productLocationTextView = itemView.findViewById(R.id.productLocationTextView);
                productImageView = itemView.findViewById(R.id.productImageView);
                editIcon = itemView.findViewById(R.id.editIcon);
                deleteIcon = itemView.findViewById(R.id.deleteIcon);
            }
        }

        private void showEditDialog(Product product) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ShowProductsActivity.this);
            builder.setTitle("Edit Product");

            LinearLayout layout = new LinearLayout(ShowProductsActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            layout.setPadding(padding, padding, padding, padding);

            EditText nameInput = new EditText(ShowProductsActivity.this);
            nameInput.setHint("Name");
            nameInput.setText(product.name);
            layout.addView(nameInput);

            EditText priceInput = new EditText(ShowProductsActivity.this);
            priceInput.setHint("Price");
            priceInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            priceInput.setText(String.valueOf(product.price));
            layout.addView(priceInput);

            EditText quantityInput = new EditText(ShowProductsActivity.this);
            quantityInput.setHint("Quantity");
            quantityInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            quantityInput.setText(String.valueOf(product.quantity));
            layout.addView(quantityInput);

            EditText descriptionInput = new EditText(ShowProductsActivity.this);
            descriptionInput.setHint("Description");
            descriptionInput.setText(product.description);
            layout.addView(descriptionInput);

            builder.setView(layout);

            builder.setPositiveButton("Update", (dialog, which) -> {
                product.name = nameInput.getText().toString().trim();
                try {
                    product.price = Double.parseDouble(priceInput.getText().toString().trim());
                    product.quantity = Double.parseDouble(quantityInput.getText().toString().trim());
                } catch (NumberFormatException e) {
                    Toast.makeText(ShowProductsActivity.this, "Invalid number format", Toast.LENGTH_SHORT).show();
                    return;
                }
                product.description = descriptionInput.getText().toString().trim();

                productsRef.child(userId).child(product.id).setValue(product)
                        .addOnSuccessListener(unused -> Toast.makeText(ShowProductsActivity.this, "Product updated", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(ShowProductsActivity.this, "Update failed", Toast.LENGTH_SHORT).show());
            });

            builder.setNegativeButton("Cancel", null);

            builder.show();
        }

        private void showDeleteConfirmation(Product product) {
            new AlertDialog.Builder(ShowProductsActivity.this)
                    .setTitle("Delete Product")
                    .setMessage("Are you sure you want to delete this product?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        productsRef.child(userId).child(product.id).removeValue()
                                .addOnSuccessListener(unused -> Toast.makeText(ShowProductsActivity.this, "Product deleted", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(ShowProductsActivity.this, "Delete failed", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("No", null)
                    .show();
        }

        private void showProductDetailsDialog(Product product) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ShowProductsActivity.this);
            builder.setTitle(product.name);

            LinearLayout layout = new LinearLayout(ShowProductsActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            int padding = (int) (16 * getResources().getDisplayMetrics().density);
            layout.setPadding(padding, padding, padding, padding);

            ImageView imageView = new ImageView(ShowProductsActivity.this);
            if (product.imageBase64 != null && !product.imageBase64.isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(product.imageBase64, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    imageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    imageView.setImageResource(R.drawable.ic_placeholder);
                }
            } else {
                imageView.setImageResource(R.drawable.ic_placeholder);
            }
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    600
            );
            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            layout.addView(imageView);

            layout.addView(createTextView("Price: " + product.price + " Taka/" + product.priceUnit));
            layout.addView(createTextView("Available: " + product.quantity + " " + product.quantityUnit));
            layout.addView(createTextView("Description: " + product.description));
            layout.addView(createTextView("Category: " + product.category));
            layout.addView(createTextView("Location: " + product.location));

            builder.setView(layout);
            builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
            builder.show();
        }


        private TextView createTextView(String text) {
            TextView tv = new TextView(ShowProductsActivity.this);
            tv.setText(text);
            tv.setPadding(0, 10, 0, 10);
            tv.setTextSize(16);
            return tv;
        }
    }

    public static class Product {
        public String id;
        public String name;
        public double price;
        public double quantity;
        public String priceUnit;
        public String quantityUnit;
        public String imageBase64;
        public String description;
        public String category;
        public String location;

        public Product() {
        }

        public Product(String name, double price, double quantity, String priceUnit, String quantityUnit,
                       String imageBase64, String description, String category, String location) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
            this.priceUnit = priceUnit;
            this.quantityUnit = quantityUnit;
            this.imageBase64 = imageBase64;
            this.description = description;
            this.category = category;
            this.location = location;
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_show_products); // Highlight current

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(this, DashboardActivity.class));
                return true;
            } else if (itemId == R.id.nav_show_products) {
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(this, ViewOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_reports) {
                startActivity(new Intent(this, SalesReportsActivity.class));
                return true;
            } else if (itemId == R.id.nav_add_product) {
                startActivity(new Intent(this, AddProductActivity.class));
                return true;
            }
            return false;
        });
    }
}
