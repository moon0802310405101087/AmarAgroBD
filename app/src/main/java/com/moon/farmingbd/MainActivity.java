package com.moon.farmingbd;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Reference the correct layout

        // Finding the Sign In button by its ID and setting up the click listener
        Button signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(v -> {
            // Redirect to SignInActivity when Sign In button is clicked
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        // Finding the Sign Up button by its ID and setting up the click listener
        Button signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(v -> {
            // Redirect to SignUpActivity when Sign Up button is clicked
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
