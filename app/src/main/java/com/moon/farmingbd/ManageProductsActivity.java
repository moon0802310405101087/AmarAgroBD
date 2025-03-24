package com.moon.farmingbd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class ManageProductsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_products);

        Button addProductButton = findViewById(R.id.addProductButton);
        Button showProductButton = findViewById(R.id.showProductButton);

        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ManageProductsActivity.this, AddProductActivity.class));
            }
        });

        showProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ManageProductsActivity.this, ShowProductsActivity.class));
            }
        });
    }
}
