package com.example.oldagehome.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.oldagehome.databinding.ActivityDoctorDashboardBinding;
import com.google.firebase.auth.FirebaseAuth;

public class DoctorDashboardActivity extends AppCompatActivity {

    private ActivityDoctorDashboardBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // For now, reuse ResidentListActivity but filter by assigned doctor
        binding.cardAssignedResidents.setOnClickListener(v -> {
            Intent intent = new Intent(this, ResidentListActivity.class);
            intent.putExtra("filter_assigned", true);
            startActivity(intent);
        });

        binding.cardAppointments.setOnClickListener(v -> startActivity(new Intent(this, AppointmentActivity.class)));

        binding.logoutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
