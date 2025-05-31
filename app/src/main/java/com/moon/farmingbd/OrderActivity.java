package com.moon.farmingbd;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class OrderActivity extends AppCompatActivity {

    private EditText quantityEditText, divisionEditText, districtEditText, cityEditText, addressEditText, phoneEditText;
    private Button orderButton;

    private FirebaseDatabase database;
    private DatabaseReference ordersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        quantityEditText = findViewById(R.id.quantityEditText);
        divisionEditText = findViewById(R.id.divisionEditText);
        districtEditText = findViewById(R.id.districtEditText);
        cityEditText = findViewById(R.id.cityEditText);
        addressEditText = findViewById(R.id.addressEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        orderButton = findViewById(R.id.placeOrderBtn);

        database = FirebaseDatabase.getInstance();
        ordersRef = database.getReference("orders");

        final String productId = getIntent().getStringExtra("productId");
        final String productName = getIntent().getStringExtra("productName");
        final String ownerId = getIntent().getStringExtra("ownerId");
        final double productPrice = getIntent().getDoubleExtra("productPrice", 0.0);  // New line to get product price

        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // customer input
                String quantityStr = quantityEditText.getText().toString();
                String division = divisionEditText.getText().toString();
                String district = districtEditText.getText().toString();
                String city = cityEditText.getText().toString();
                String address = addressEditText.getText().toString();
                String phone = phoneEditText.getText().toString();

                if (quantityStr.isEmpty() || division.isEmpty() || district.isEmpty() || city.isEmpty() || address.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(OrderActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                    return;
                }

                int quantity = Integer.parseInt(quantityStr);
                String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                // Calculate the total price
                double totalPrice = quantity * productPrice;


                Order order = new Order();
                order.setProductId(productId);
                order.setProductName(productName);
                order.setOwnerId(ownerId);
                order.setCustomerId(customerId);
                order.setQuantity(quantity);
                order.setDivision(division);
                order.setDistrict(district);
                order.setCity(city);
                order.setAddress(address);
                order.setPhone(phone);
                order.setTimestamp(System.currentTimeMillis());
                order.setTotalPrice(totalPrice);
                order.setOrderStatus("Pending");

                // Push
                ordersRef.push().setValue(order)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(OrderActivity.this, "Order placed successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(OrderActivity.this, "Failed to place order!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
