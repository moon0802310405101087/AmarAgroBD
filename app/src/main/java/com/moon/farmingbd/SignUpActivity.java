package com.moon.farmingbd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button signUpButton, signInRedirectButton;
    private Spinner roleSpinner;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up); // Ensure this is correctly linked

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        signUpButton = findViewById(R.id.signUpButton);
        signInRedirectButton = findViewById(R.id.signInRedirectButton);
        roleSpinner = findViewById(R.id.roleSpinner);

        // Set up the Spinner for role selection
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.roles_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        // Set OnClickListener for Sign Up button
        signUpButton.setOnClickListener(v -> signUpUser());

        // Set OnClickListener for Sign In redirect button
        signInRedirectButton.setOnClickListener(v -> redirectToSignIn());
    }

    private void signUpUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String role = roleSpinner.getSelectedItem().toString(); // Get selected role

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(SignUpActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();

                        // Save the user role in Firestore
                        saveUserRole(user, role);

                        // Redirect to Sign In or main activity if needed
                        redirectToSignIn();
                    } else {
                        // If sign up fails, display a message to the user.
                        Toast.makeText(SignUpActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserRole(FirebaseUser user, String role) {
        // Save the user's role in Firestore under a collection called 'users'
        if (user != null) {
            String userId = user.getUid();
            db.collection("users").document(userId)
                    .set(new User(user.getEmail(), role))
                    .addOnSuccessListener(aVoid -> {
                        // Successfully saved user role to Firestore
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SignUpActivity.this, "Failed to save role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void redirectToSignIn() {
        // Redirect to SignInActivity if user clicks "Sign In"
        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
        startActivity(intent);
        finish();
    }
}
