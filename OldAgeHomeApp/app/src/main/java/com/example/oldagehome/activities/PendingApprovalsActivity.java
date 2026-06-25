package com.example.oldagehome.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.oldagehome.adapters.PendingResidentAdapter;
import com.example.oldagehome.databinding.ActivityPendingApprovalsBinding;
import com.example.oldagehome.models.ResidentModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class PendingApprovalsActivity extends AppCompatActivity {

    private ActivityPendingApprovalsBinding binding;
    private FirebaseFirestore db;
    private PendingResidentAdapter adapter;
    private List<ResidentModel> pendingResidents;
    private com.example.oldagehome.adapters.ResidentAdapter approvedAdapter;
    private List<ResidentModel> approvedResidents;
    private String communityId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPendingApprovalsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        pendingResidents = new ArrayList<>();
        approvedResidents = new ArrayList<>();

        // Setup Pending List
        binding.rvPendingResidents.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PendingResidentAdapter(pendingResidents);
        binding.rvPendingResidents.setAdapter(adapter);

        // Setup Approved List
        binding.rvApprovedResidents.setLayoutManager(new LinearLayoutManager(this));
        approvedAdapter = new com.example.oldagehome.adapters.ResidentAdapter(approvedResidents);
        binding.rvApprovedResidents.setAdapter(approvedAdapter);

        binding.btnBack.setOnClickListener(v -> onBackPressed());

        fetchCommunityAndResidents();
    }

    private void fetchCommunityAndResidents() {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                communityId = documentSnapshot.getString("communityId");
                if (communityId != null) {
                    fetchPendingResidents();
                    fetchApprovedResidents();
                } else {
                    db.collection("communities").whereEqualTo("creatorUid", uid).get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                    com.google.firebase.firestore.DocumentSnapshot commDoc = querySnapshot.getDocuments().get(0);
                                    communityId = commDoc.getId();
                                    String communityName = commDoc.getString("name");
                                    if (communityId != null) {
                                        java.util.Map<String, Object> updates = new java.util.HashMap<>();
                                        updates.put("communityId", communityId);
                                        if (communityName != null) {
                                            updates.put("communityName", communityName);
                                        }
                                        db.collection("users").document(uid).update(updates);
                                        fetchPendingResidents();
                                        fetchApprovedResidents();
                                    }
                                }
                            });
                }
            }
        });
    }

    private void fetchPendingResidents() {
        db.collection("communities").document(communityId)
                .collection("members")
                .whereEqualTo("status", "pending")
                .whereEqualTo("role", com.example.oldagehome.utils.RoleManager.ROLE_RESIDENT)
                .addSnapshotListener((value, error) -> {
                    if (error != null)
                        return;

                    pendingResidents.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            ResidentModel resident = doc.toObject(ResidentModel.class);
                            resident.setId(doc.getId());
                            pendingResidents.add(resident);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (pendingResidents.isEmpty()) {
                        binding.tvNoPending.setVisibility(android.view.View.VISIBLE);
                    } else {
                        binding.tvNoPending.setVisibility(android.view.View.GONE);
                    }
                });
    }

    private void fetchApprovedResidents() {
        db.collection("communities").document(communityId)
                .collection("members")
                .whereEqualTo("status", "approved")
                .whereEqualTo("role", com.example.oldagehome.utils.RoleManager.ROLE_RESIDENT)
                .addSnapshotListener((value, error) -> {
                    if (error != null)
                        return;

                    approvedResidents.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            ResidentModel resident = doc.toObject(ResidentModel.class);
                            resident.setId(doc.getId());
                            approvedResidents.add(resident);
                        }
                    }
                    approvedAdapter.notifyDataSetChanged();
                });
    }
}
