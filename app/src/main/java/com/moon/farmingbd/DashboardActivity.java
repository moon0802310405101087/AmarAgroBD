package com.moon.farmingbd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private Button manageProductsButton;
    private Button viewOrdersButton;
    private Button viewDeliveryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard); // Use the correct layout file

        // Initialize buttons
        manageProductsButton = findViewById(R.id.manageProductsButton);
        viewOrdersButton = findViewById(R.id.viewOrdersButton);
        viewDeliveryButton = findViewById(R.id.viewDeliveryButton);

        // Set onClick listeners for each button
        manageProductsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Manage Products Activity
                Intent intent = new Intent(DashboardActivity.this, ManageProductsActivity.class);
                startActivity(intent);
            }
        });

        viewOrdersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open View Orders Activity
                Intent intent = new Intent(DashboardActivity.this, ViewOrdersActivity.class);
                startActivity(intent);
            }
        });

        viewDeliveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open View Deliveries Activity
                Intent intent = new Intent(DashboardActivity.this, ViewDeliveriesActivity.class);
                startActivity(intent);
            }
        });
    }
}
