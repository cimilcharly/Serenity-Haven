package com.example.oldagehome.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.oldagehome.databinding.ActivityForgotPasswordBinding;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnReset.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = binding.etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            binding.etEmail.setError("Email is required");
            binding.etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Please enter a valid email");
            binding.etEmail.requestFocus();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.btnReset.setEnabled(false);

        // First check if user exists in Firestore to provide better feedback
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnReset.setEnabled(true);
                        Toast.makeText(this,
                                "Email not found in our records. Please check the spelling or register first.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        // User exists in DB, now attempt Auth reset
                        sendAuthResetLink(email);
                    }
                })
                .addOnFailureListener(e -> {
                    // If DB check fails, try Auth anyway
                    sendAuthResetLink(email);
                });
    }

    private void sendAuthResetLink(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnReset.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Reset Link Sent! Please check your Inbox and Spam folder.",
                                Toast.LENGTH_LONG).show();
                        new android.os.Handler().postDelayed(this::finish, 2000);
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
