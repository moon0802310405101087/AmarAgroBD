package com.moon.farmingbd;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class ViewProductsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<ProductWithOwner> productList;

    private FirebaseDatabase database;
    private DatabaseReference productsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_products);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList, this);
        recyclerView.setAdapter(productAdapter);

        database = FirebaseDatabase.getInstance();
        productsRef = database.getReference("products");

        fetchAllProducts();
    }

    private void fetchAllProducts() {
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productList.clear();

                for (DataSnapshot ownerSnapshot : dataSnapshot.getChildren()) {
                    String ownerId = ownerSnapshot.getKey();

                    for (DataSnapshot productSnapshot : ownerSnapshot.getChildren()) {
                        try {
                            Product product = productSnapshot.getValue(Product.class);
                            if (product != null) {
                                product.setId(productSnapshot.getKey());
                                product.setOwnerId(ownerId);
                                productList.add(new ProductWithOwner(product, ownerId));
                            }
                        } catch (Exception e) {
                            Log.e("FirebaseFix", "Error reading product: " + e.getMessage());
                        }
                    }
                }

                productAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ViewProductsActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class ProductWithOwner {
        public Product product;
        public String ownerId;

        public ProductWithOwner(Product product, String ownerId) {
            this.product = product;
            this.ownerId = ownerId;
        }
    }

    public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

        private List<ProductWithOwner> productList;
        private Context context;

        public ProductAdapter(List<ProductWithOwner> productList, Context context) {
            this.productList = productList;
            this.context = context;
        }

        @Override
        public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_customer, parent, false);
            return new ProductViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ProductViewHolder holder, int position) {
            ProductWithOwner pwo = productList.get(position);
            Product product = pwo.product;

            holder.productNameTextView.setText(product.getName());
            holder.productPriceTextView.setText("Price: " + product.getPrice() + " " + product.getPriceUnit());
            holder.productQuantityTextView.setText("Available: " + product.getQuantity() + " " + product.getQuantityUnit());
            holder.ownerIdTextView.setText("Owner ID: " + pwo.ownerId);
            holder.categoryTextView.setText("Category: " + product.getCategory());
            holder.locationTextView.setText("Location: " + product.getLocation());
            holder.descriptionTextView.setText("Description: " + product.getDescription());

            String base64Image = product.getImageBase64();
            if (base64Image != null && !base64Image.isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    holder.productImageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    holder.productImageView.setImageResource(R.drawable.ic_placeholder);
                }
            } else {
                holder.productImageView.setImageResource(R.drawable.ic_placeholder);
            }


            holder.itemView.setOnClickListener(v -> {

                View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_product_detail, null);

                ImageView dialogImage = dialogView.findViewById(R.id.dialogProductImage);
                TextView dialogName = dialogView.findViewById(R.id.dialogProductName);
                TextView dialogPrice = dialogView.findViewById(R.id.dialogProductPrice);
                TextView dialogQuantity = dialogView.findViewById(R.id.dialogProductQuantity);
                TextView dialogOwnerId = dialogView.findViewById(R.id.dialogOwnerId);
                TextView dialogCategory = dialogView.findViewById(R.id.dialogCategory);
                TextView dialogLocation = dialogView.findViewById(R.id.dialogLocation);
                TextView dialogDescription = dialogView.findViewById(R.id.dialogDescription);

                // Set values
                dialogName.setText(product.getName());
                dialogPrice.setText("Price: " + product.getPrice() + " " + product.getPriceUnit());
                dialogQuantity.setText("Available: " + product.getQuantity() + " " + product.getQuantityUnit());
                dialogOwnerId.setText("Owner ID: " + pwo.ownerId);
                dialogCategory.setText("Category: " + product.getCategory());
                dialogLocation.setText("Location: " + product.getLocation());
                dialogDescription.setText("Description: " + product.getDescription());

                if (base64Image != null && !base64Image.isEmpty()) {
                    try {
                        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        dialogImage.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                        dialogImage.setImageResource(R.drawable.ic_placeholder);
                    }
                } else {
                    dialogImage.setImageResource(R.drawable.ic_placeholder);
                }


                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setView(dialogView)
                        .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                        .show();
            });

            holder.orderNowBtn.setOnClickListener(v -> {
                Intent intent = new Intent(ViewProductsActivity.this, OrderActivity.class);

                intent.putExtra("productId", product.getId());
                intent.putExtra("productName", product.getName());
                intent.putExtra("productPrice", product.getPrice());
                intent.putExtra("ownerId", product.getOwnerId()); // Pass ownerId

                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return productList.size();
        }

        public class ProductViewHolder extends RecyclerView.ViewHolder {
            ImageView productImageView;
            TextView productNameTextView, productPriceTextView, productQuantityTextView;
            TextView ownerIdTextView, categoryTextView, locationTextView, descriptionTextView;
            Button orderNowBtn;

            public ProductViewHolder(View itemView) {
                super(itemView);
                productImageView = itemView.findViewById(R.id.productImageView);
                productNameTextView = itemView.findViewById(R.id.productNameTextView);
                productPriceTextView = itemView.findViewById(R.id.productPriceTextView);
                productQuantityTextView = itemView.findViewById(R.id.productQuantityTextView);
                ownerIdTextView = itemView.findViewById(R.id.productOwnerTextView);
                categoryTextView = itemView.findViewById(R.id.productCategoryTextView);
                locationTextView = itemView.findViewById(R.id.productLocationTextView);
                descriptionTextView = itemView.findViewById(R.id.productDescriptionTextView);
                orderNowBtn = itemView.findViewById(R.id.orderNowBtn);
            }
        }
    }
}
