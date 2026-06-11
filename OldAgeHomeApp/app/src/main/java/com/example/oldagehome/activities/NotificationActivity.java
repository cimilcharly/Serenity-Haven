package com.example.oldagehome.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.oldagehome.adapters.NotificationAdapter;
import com.example.oldagehome.databinding.ActivityNotificationBinding;
import com.example.oldagehome.models.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private ActivityNotificationBinding binding;
    private NotificationAdapter adapter;
    private List<NotificationModel> notificationList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        if (auth.getCurrentUser() == null)
            return;

        String uid = auth.getCurrentUser().getUid();
        // In real app, filter where recipientId == uid
        db.collection("notifications")
                .whereEqualTo("recipientId", uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        notificationList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            NotificationModel notif = document.toObject(NotificationModel.class);
                            notif.setId(document.getId());
                            notificationList.add(notif);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
