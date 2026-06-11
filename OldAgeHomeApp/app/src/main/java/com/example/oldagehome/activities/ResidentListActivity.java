package com.example.oldagehome.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.oldagehome.adapters.ResidentAdapter;
import com.example.oldagehome.databinding.ActivityResidentListBinding;
import com.example.oldagehome.models.ResidentModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ResidentListActivity extends AppCompatActivity {

    private ActivityResidentListBinding binding;
    private ResidentAdapter adapter;
    private List<ResidentModel> residentList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResidentListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        residentList = new ArrayList<>();
        adapter = new ResidentAdapter(residentList);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new ResidentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ResidentModel resident) {
                Intent intent = new Intent(ResidentListActivity.this, MedicineActivity.class);
                intent.putExtra("residentId", resident.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(ResidentModel resident) {
                // Not handled here
            }
        });

        binding.fabAdd.setOnClickListener(v -> startActivity(new Intent(this, AddResidentActivity.class)));

        loadResidents();
    }

    private void loadResidents() {
        db.collection("users").whereEqualTo("role", com.example.oldagehome.utils.RoleManager.ROLE_RESIDENT).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        residentList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ResidentModel resident = document.toObject(ResidentModel.class);
                            resident.setId(document.getId());
                            residentList.add(resident);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
