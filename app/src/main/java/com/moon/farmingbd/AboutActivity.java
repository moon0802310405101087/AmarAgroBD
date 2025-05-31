package com.moon.farmingbd;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView aboutText = findViewById(R.id.aboutTextView);
        aboutText.setText(
                "AmarAgroBD\n\n" +
                        "Version 1.0\n\n" +
                        "It is a user-friendly mobile app designed to simplify the buying and selling of farming products. " +
                        "It connects farmers, sellers, and customers across Bangladesh, providing a convenient platform to browse, " +
                        "order, and manage agricultural goods.\n\n" +
                        "Key features include:\n" +
                        "- Easy product listing for sellers and farmers\n" +
                        "- Real-time updates on product availability and delivery status\n\n" +
                        "Our mission is to empower the agricultural community by bridging the gap between producers and buyers, " +
                        "helping to boost the local farming economy and improve access to fresh, quality products.\n\n" +
                        "Developed by:\n" +
                        "SM Mahamudul Hasan Moon\n" +
                        "Department: CSE\n" +
                        "Batch: 16"
        );
    }
}
