package com.moon.farmingbd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignInActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        Button signInButton = findViewById(R.id.signInButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        signInButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignInActivity.this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Sign-in logic
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            fetchUserRoleAndRedirect(userId);
                        } else {
                            Toast.makeText(SignInActivity.this, "Sign In Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void fetchUserRoleAndRedirect(String userId) {
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        DocumentSnapshot document = task.getResult();
                        String role = document.getString("role");

                        // Debugging: Show role in Toast and Logcat
                        Toast.makeText(SignInActivity.this, "User Role: " + role, Toast.LENGTH_LONG).show();
                        Log.d("ROLE_DEBUG", "Fetched Role: " + role);

                        if (role != null) {
                            redirectToDashboard(role);
                        } else {
                            Toast.makeText(SignInActivity.this, "Role not found!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignInActivity.this, "Failed to fetch role", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void redirectToDashboard(String role) {
        Intent intent;
        switch (role) {
            case "Owner":
                intent = new Intent(SignInActivity.this, DashboardActivity.class);
                break;
            case "Customer":
                intent = new Intent(SignInActivity.this, CustomerDashboardActivity.class);
                break;
            case "DeliveryMan":
                intent = new Intent(SignInActivity.this, DeliveryDashboardActivity.class);
                break;
            default:
                intent = new Intent(SignInActivity.this, MainActivity.class); // Fallback
                break;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
