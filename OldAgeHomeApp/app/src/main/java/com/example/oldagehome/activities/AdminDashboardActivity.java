package com.example.oldagehome.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.oldagehome.databinding.ActivityAdminDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;

public class AdminDashboardActivity extends AppCompatActivity {

    private ActivityAdminDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.cardResidents.setOnClickListener(v -> startActivity(new Intent(this, ResidentListActivity.class)));
        binding.cardDoctors.setOnClickListener(v -> startActivity(new Intent(this, DoctorListActivity.class)));
        // binding.cardStaff.setOnClickListener(v -> startActivity(new Intent(this,
        // StaffListActivity.class))); // Example
        binding.cardAppointments.setOnClickListener(v -> startActivity(new Intent(this, AppointmentActivity.class)));

        binding.logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
