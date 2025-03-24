package com.moon.farmingbd;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class DeliveryDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_dashboard);

        Button manageDeliveriesButton = findViewById(R.id.manageDeliveriesButton);
        Button confirmDeliveryButton = findViewById(R.id.confirmDeliveryButton);

        manageDeliveriesButton.setOnClickListener(v -> {
            // TODO: Implement Manage Deliveries feature
        });

        confirmDeliveryButton.setOnClickListener(v -> {
            // TODO: Implement Confirm Deliveries feature
        });
    }
}
