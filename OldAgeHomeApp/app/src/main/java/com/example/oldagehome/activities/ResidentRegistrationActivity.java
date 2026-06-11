package com.example.oldagehome.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.oldagehome.databinding.ActivityResidentRegistrationBinding;
import com.example.oldagehome.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import android.content.Intent;

public class ResidentRegistrationActivity extends AppCompatActivity {

    private ActivityResidentRegistrationBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private com.google.firebase.storage.StorageReference storageReference;
    private android.net.Uri imageUri;

    private final androidx.activity.result.ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    binding.ivProfile.setImageURI(imageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResidentRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = com.google.firebase.storage.FirebaseStorage.getInstance().getReference("profile_images");

        binding.ivProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImage.launch(intent);
        });

        binding.btnRegister.setOnClickListener(v -> registerResident());
    }

    private void registerResident() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String ageStr = binding.etAge.getText().toString().trim();
        String gender = binding.etGender.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String enteredJoinCode = binding.etCommunity.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || ageStr.isEmpty() || password.isEmpty() || enteredJoinCode.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate Join Code first
        db.collection("communities")
                .whereEqualTo("joinCode", enteredJoinCode.toUpperCase())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        com.google.firebase.firestore.DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                        String communityId = doc.getId();
                        String communityName = doc.getString("name");

                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(authTask -> {
                                    if (authTask.isSuccessful()) {
                                        String uid = mAuth.getCurrentUser().getUid();
                                        if (imageUri != null) {
                                            uploadImage(uid, name, email, ageStr, gender, communityId, communityName);
                                        } else {
                                            createUser(uid, name, email, ageStr, gender, null, communityId, communityName);
                                        }
                                    } else {
                                        Toast.makeText(this, "Registration failed: " + authTask.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Invalid Join Code. Please check with your staff.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void uploadImage(String uid, String name, String email, String ageStr, String gender,
            String communityId, String communityName) {
        com.google.firebase.storage.StorageReference fileRef = storageReference.child(uid + ".jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    createUser(uid, name, email, ageStr, gender, uri.toString(), communityId, communityName);
                })).addOnFailureListener(e -> {
                    Toast.makeText(this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    createUser(uid, name, email, ageStr, gender, null, communityId, communityName);
                });
    }

    private void createUser(String uid, String name, String email, String ageStr, String gender, String imageUrl,
            String communityId, String communityName) {
        int age = Integer.parseInt(ageStr);

        UserModel user = new UserModel();
        user.setUid(uid);
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);
        user.setGender(gender);
        user.setCommunityName(communityName);
        user.setCommunityId(communityId);
        user.setRole(com.example.oldagehome.utils.RoleManager.ROLE_RESIDENT);
        user.setStatus("pending");
        user.setCreatedBy(com.example.oldagehome.utils.RoleManager.ROLE_RESIDENT);
        user.setAdmissionDate(System.currentTimeMillis());
        user.setMedicines(new ArrayList<>());
        if (imageUrl != null) {
            user.setProfileImageUrl(imageUrl);
        }

        // 1. Global Registry (for login)
        java.util.Map<String, Object> globalUser = new java.util.HashMap<>();
        globalUser.put("uid", uid);
        globalUser.put("email", email);
        globalUser.put("role", com.example.oldagehome.utils.RoleManager.ROLE_RESIDENT);
        globalUser.put("communityName", communityName);
        globalUser.put("communityId", communityId);
        globalUser.put("status", "pending");

        db.collection("users").document(uid).set(globalUser)
                .addOnSuccessListener(aVoid -> {
                    // 2. Private Community Database
                    db.collection("communities").document(communityId)
                            .collection("members").document(uid).set(user)
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "Join Request sent to " + communityName, Toast.LENGTH_SHORT)
                                        .show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
