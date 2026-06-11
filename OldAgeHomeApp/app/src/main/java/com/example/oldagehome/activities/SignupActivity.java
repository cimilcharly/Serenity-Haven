package com.example.oldagehome.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.oldagehome.R;
import com.example.oldagehome.databinding.ActivitySignupBinding;
import com.example.oldagehome.models.UserModel;
import com.example.oldagehome.utils.RoleManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
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
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = com.google.firebase.storage.FirebaseStorage.getInstance().getReference("profile_images");

        // Setup Role Spinner
        String[] roles = { RoleManager.ROLE_RESIDENT, RoleManager.ROLE_STAFF };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.roleSpinner.setAdapter(adapter);

        binding.roleSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedRole = roles[position];
                if (RoleManager.ROLE_RESIDENT.equals(selectedRole)) {
                    binding.communityInputLayout.setHint("Community Join Code");
                } else {
                    binding.communityInputLayout.setHint("Community Name");
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        binding.ivProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickImage.launch(intent);
        });

        binding.signupBtn.setOnClickListener(v -> registerUser());
        binding.loginTv.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
    }

    interface OnAuthUserCreatedListener {
        void onCreated(String uid);
    }

    private void createFirebaseAuthUser(OnAuthUserCreatedListener listener, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        listener.onCreated(mAuth.getCurrentUser().getUid());
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Registration Failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String generateJoinCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private void registerUser() {
        String name = binding.nameEt.getText().toString().trim();
        String email = binding.emailEt.getText().toString().trim();
        String password = binding.passwordEt.getText().toString().trim();
        String role = binding.roleSpinner.getSelectedItem().toString();
        String communityInput = binding.communityEt.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || communityInput.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);

        if (RoleManager.ROLE_RESIDENT.equals(role)) {
            // Validate join code first
            db.collection("communities")
                    .whereEqualTo("joinCode", communityInput.toUpperCase())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                            com.google.firebase.firestore.DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                            String communityId = doc.getId();
                            String communityName = doc.getString("name");

                            createFirebaseAuthUser(uid -> {
                                if (imageUri != null) {
                                    uploadImage(uid, name, email, role, communityId, communityName, null);
                                } else {
                                    saveUserToFirestore(uid, name, email, role, null, communityId, communityName, null);
                                }
                            }, email, password);
                        } else {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Invalid Join Code. Please check with your staff.", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            // Main Staff: Create new community
            String communityId = db.collection("communities").document().getId();
            String joinCode = generateJoinCode();

            createFirebaseAuthUser(uid -> {
                Map<String, Object> communityData = new java.util.HashMap<>();
                communityData.put("id", communityId);
                communityData.put("name", communityInput);
                communityData.put("creatorUid", uid);
                communityData.put("joinCode", joinCode);
                communityData.put("createdAt", System.currentTimeMillis());

                db.collection("communities").document(communityId).set(communityData)
                        .addOnSuccessListener(aVoid -> {
                            if (imageUri != null) {
                                uploadImage(uid, name, email, role, communityId, communityInput, joinCode);
                            } else {
                                saveUserToFirestore(uid, name, email, role, null, communityId, communityInput, joinCode);
                            }
                        })
                        .addOnFailureListener(e -> {
                            binding.progressBar.setVisibility(View.GONE);
                            Toast.makeText(this, "Failed to create community: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }, email, password);
        }
    }

    private void uploadImage(String uid, String name, String email, String role, String communityId, String communityName, String joinCode) {
        com.google.firebase.storage.StorageReference fileRef = storageReference.child(uid + ".jpg");
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    saveUserToFirestore(uid, name, email, role, uri.toString(), communityId, communityName, joinCode);
                })).addOnFailureListener(e -> {
                    Toast.makeText(this, "Image Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    saveUserToFirestore(uid, name, email, role, null, communityId, communityName, joinCode);
                });
    }

    private void saveUserToFirestore(String uid, String name, String email, String role, String imageUrl,
            String communityId, String communityName, String joinCode) {
        UserModel user = new UserModel(uid, email, role, name);
        user.setCommunityName(communityName);
        user.setCommunityId(communityId);
        if (imageUrl != null) {
            user.setProfileImageUrl(imageUrl);
        }
        user.setStatus(RoleManager.ROLE_RESIDENT.equals(role) ? "pending" : "approved");
        if (RoleManager.ROLE_RESIDENT.equals(role)) {
            user.setAdmissionDate(System.currentTimeMillis());
        }

        // 1. Separate "Global Directory" (used for login and finding the community)
        Map<String, Object> globalUser = new HashMap<>();
        globalUser.put("uid", uid);
        globalUser.put("email", email);
        globalUser.put("role", role);
        globalUser.put("communityName", communityName);
        globalUser.put("communityId", communityId);
        globalUser.put("status", user.getStatus());

        db.collection("users").document(uid).set(globalUser)
                .addOnSuccessListener(aVoid -> {
                    // 2. Separate "Community Vault" (Private home data)
                    db.collection("communities").document(communityId)
                            .collection("members").document(uid).set(user)
                            .addOnSuccessListener(aVoid2 -> {
                                binding.progressBar.setVisibility(View.GONE);
                                if (RoleManager.ROLE_RESIDENT.equals(role)) {
                                    Toast.makeText(this, "Join Request sent to " + communityName, Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(this, LoginActivity.class));
                                    finish();
                                } else {
                                    new androidx.appcompat.app.AlertDialog.Builder(this)
                                            .setTitle("Community Created!")
                                            .setMessage("Your community has been created successfully.\n\nShare this Join Code with residents:\n" + joinCode)
                                            .setPositiveButton("OK", (dialog, which) -> {
                                                startActivity(new Intent(this, LoginActivity.class));
                                                finish();
                                            })
                                            .setCancelable(false)
                                            .show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
