package com.moon.farmingbd;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class DeliveryDashboardActivity extends AppCompatActivity {

    private Button manageDeliveriesButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_dashboard);

        manageDeliveriesButton = findViewById(R.id.manageDeliveriesButton);
        logoutButton = findViewById(R.id.logoutButton);

        manageDeliveriesButton.setOnClickListener(v -> {
            Intent intent = new Intent(DeliveryDashboardActivity.this, DeliveryOrdersActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(DeliveryDashboardActivity.this, MainActivity.class); // Replace with your login activity
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
