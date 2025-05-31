package com.moon.farmingbd;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextView emailTextView;
    private EditText nameEditText, phoneEditText;
    private MaterialButton editSaveButton, backButton, btnSelectImage;
    private ImageView profileImageView;

    private FirebaseUser currentUser;
    private DatabaseReference userRef;

    private boolean isEditMode = false;
    private String profileImageBase64 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        emailTextView = findViewById(R.id.emailTextView);
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        editSaveButton = findViewById(R.id.editSaveButton);
        backButton = findViewById(R.id.backButton);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        profileImageView = findViewById(R.id.profileImageView);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        emailTextView.setText(currentUser.getEmail());

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());

        loadUserProfile();

        btnSelectImage.setOnClickListener(v -> {
            if (isEditMode) {
                openImageChooser();
            } else {
                Toast.makeText(this, "Enable edit mode to change image", Toast.LENGTH_SHORT).show();
            }
        });

        editSaveButton.setOnClickListener(v -> {
            if (isEditMode) {
                saveUserProfile();
            } else {
                setEditMode(true);
            }
        });

        backButton.setOnClickListener(v -> finish());

        setEditMode(false);
    }

    private void loadUserProfile() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String phone = snapshot.child("phone").getValue(String.class);
                    String name = snapshot.child("name").getValue(String.class);
                    String imageBase64 = snapshot.child("profileImage").getValue(String.class);

                    nameEditText.setText(name != null ? name : "");
                    phoneEditText.setText(phone != null ? phone : "");

                    if (imageBase64 != null && !imageBase64.isEmpty()) {
                        Bitmap bitmap = base64ToBitmap(imageBase64);
                        profileImageView.setImageBitmap(bitmap);
                        profileImageBase64 = imageBase64;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserProfile() {
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Name is required");
            nameEditText.requestFocus();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("phone", phone);
        updates.put("name", name);
        updates.put("profileImage", profileImageBase64);

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                setEditMode(false);
            } else {
                Toast.makeText(ProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setEditMode(boolean enabled) {
        isEditMode = enabled;
        nameEditText.setEnabled(enabled);
        phoneEditText.setEnabled(enabled);

        if (enabled) {
            editSaveButton.setText("Save");
            btnSelectImage.setVisibility(View.VISIBLE);
        } else {
            editSaveButton.setText("Edit");
            btnSelectImage.setVisibility(View.GONE);
        }
    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(inputStream);

                profileImageView.setImageBitmap(selectedImage);

                profileImageBase64 = bitmapToBase64(selectedImage);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap base64ToBitmap(String base64Str) {
        byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
