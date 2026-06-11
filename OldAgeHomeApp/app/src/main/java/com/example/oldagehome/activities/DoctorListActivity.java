package com.example.oldagehome.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.oldagehome.adapters.DoctorAdapter;
import com.example.oldagehome.databinding.ActivityDoctorListBinding;
import com.example.oldagehome.models.DoctorModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class DoctorListActivity extends AppCompatActivity {

    private ActivityDoctorListBinding binding;
    private DoctorAdapter adapter;
    private List<DoctorModel> doctorList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDoctorListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        doctorList = new ArrayList<>();
        adapter = new DoctorAdapter(doctorList);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.fabAdd.setOnClickListener(v -> {
            // Open AddDoctorActivity (To be implemented or just stubbed)
            // startActivity(new Intent(this, AddDoctorActivity.class));
        });

        loadDoctors();
    }

    private void loadDoctors() {
        db.collection("doctors").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        doctorList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            DoctorModel doctor = document.toObject(DoctorModel.class);
                            doctor.setId(document.getId());
                            doctorList.add(doctor);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
