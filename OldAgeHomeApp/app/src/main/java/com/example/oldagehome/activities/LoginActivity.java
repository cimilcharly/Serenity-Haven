package com.example.oldagehome.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.oldagehome.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Notifications disabled. You might miss important alerts.", Toast.LENGTH_LONG)
                            .show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        askNotificationPermission();

        if (mAuth.getCurrentUser() != null) {
            checkUserRole(mAuth.getCurrentUser().getUid());
        }

        binding.loginBtn.setOnClickListener(v -> loginUser());
        binding.signupTv.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        binding.forgotPasswordTv.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void loginUser() {
        String email = binding.emailEt.getText().toString().trim();
        String password = binding.passwordEt.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        checkUserRole(mAuth.getCurrentUser().getUid());
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void checkUserRole(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        String status = documentSnapshot.getString("status");

                        if ("pending".equals(status)) {
                            Toast.makeText(this, "Your registration is pending approval.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        } else if (status == null || "approved".equals(status)) {
                            navigateToDashboard(role);
                        } else {
                            Toast.makeText(this, "Invalid user status.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if (com.example.oldagehome.utils.RoleManager.ROLE_STAFF.equals(role)) {
            intent = new Intent(this, MainStaffDashboardActivity.class);
        } else if (com.example.oldagehome.utils.RoleManager.ROLE_RESIDENT.equals(role)) {
            intent = new Intent(this, ResidentDashboardActivity.class);
        } else {
            Toast.makeText(this, "Role not recognized: " + role, Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);
        finish();
    }
}
