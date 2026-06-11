package com.example.oldagehome.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.oldagehome.databinding.ActivityStaffDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;

public class StaffDashboardActivity extends AppCompatActivity {

    private ActivityStaffDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStaffDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.cardResidents.setOnClickListener(v -> startActivity(new Intent(this, ResidentListActivity.class)));
        // binding.cardDoctorSchedule.setOnClickListener(v -> startActivity(new
        // Intent(this, DoctorScheduleActivity.class))); // TODO

        binding.logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
