package com.example.oldagehome.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.oldagehome.adapters.AppointmentAdapter;
import com.example.oldagehome.databinding.ActivityAppointmentBinding;
import com.example.oldagehome.models.AppointmentModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AppointmentActivity extends AppCompatActivity {

    private ActivityAppointmentBinding binding;
    private AppointmentAdapter adapter;
    private List<AppointmentModel> appointmentList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAppointmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        appointmentList = new ArrayList<>();
        adapter = new AppointmentAdapter(appointmentList);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.btnBook.setOnClickListener(v -> {
            // Trigger logic to book appointment
            // For simplicity, we'll just show a toast or a dialog
            // In a real app, open a DialogFragment or Activity to pick date/time/doctor
            createTestAppointment();
        });

        loadAppointments();
    }

    private void createTestAppointment() {
        String id = db.collection("appointments").document().getId();
        // Mock data
        AppointmentModel appointment = new AppointmentModel(id, "Resident1", "Doctor1", System.currentTimeMillis(),
                "Routine Checkup", "Pending");

        db.collection("appointments").document(id).set(appointment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Appointment Booked", Toast.LENGTH_SHORT).show();
                    loadAppointments();
                });
    }

    private void loadAppointments() {
        // In a real app, filter by user role (if Doctor, show assigned; if Admin, show
        // all)
        db.collection("appointments").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        appointmentList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            AppointmentModel appt = document.toObject(AppointmentModel.class);
                            appointmentList.add(appt);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
