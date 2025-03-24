package com.moon.farmingbd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class CustomerDashboardActivity extends AppCompatActivity {

    private Button orderProductsButton, viewOrderHistoryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        orderProductsButton = findViewById(R.id.orderProductsButton);
        viewOrderHistoryButton = findViewById(R.id.viewOrderHistoryButton);

        // Navigate to OrderProductsActivity
        orderProductsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerDashboardActivity.this, OrderProductsActivity.class);
                startActivity(intent);
            }
        });

        // Navigate to ViewOrdersActivity
        viewOrderHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomerDashboardActivity.this, ViewOrdersActivity.class);
                startActivity(intent);
            }
        });
    }
}
