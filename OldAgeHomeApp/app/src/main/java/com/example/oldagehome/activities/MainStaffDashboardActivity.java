package com.example.oldagehome.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.oldagehome.R;
import com.example.oldagehome.adapters.ResidentAdapter;
import com.example.oldagehome.databinding.ActivityMainStaffDashboardBinding;
import com.example.oldagehome.models.ResidentModel;
import com.example.oldagehome.utils.RoleManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import com.example.oldagehome.models.MedicineModel;
import com.example.oldagehome.utils.AlarmReceiver;

public class MainStaffDashboardActivity extends AppCompatActivity {

    private ActivityMainStaffDashboardBinding binding;
    private ResidentAdapter staffAdapter, selfAdapter;
    private List<ResidentModel> staffResidentList, selfResidentList, allResidents;
    private FirebaseFirestore db;
    private String communityId = null;
    private String communityName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainStaffDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        db = FirebaseFirestore.getInstance();
        staffResidentList = new ArrayList<>();
        selfResidentList = new ArrayList<>();
        allResidents = new ArrayList<>();

        staffAdapter = new ResidentAdapter(staffResidentList);
        selfAdapter = new ResidentAdapter(selfResidentList);

        setupAdapter(staffAdapter);
        setupAdapter(selfAdapter);

        binding.rvStaffResidents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvStaffResidents.setAdapter(staffAdapter);

        binding.rvSelfResidents.setLayoutManager(new LinearLayoutManager(this));
        binding.rvSelfResidents.setAdapter(selfAdapter);

        binding.cardApprove.setOnClickListener(v -> {
            startActivity(new Intent(this, PendingApprovalsActivity.class));
        });

        binding.tvLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        binding.fabAddResident.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddResidentActivity.class);
            intent.putExtra("communityId", communityId);
            intent.putExtra("communityName", communityName);
            startActivity(intent);
        });

        loadResidents();
    }

    private void setupAdapter(ResidentAdapter adapter) {
        adapter.setOnItemClickListener(new ResidentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ResidentModel resident) {
                Intent intent = new Intent(MainStaffDashboardActivity.this, MedicineActivity.class);
                intent.putExtra("residentId", resident.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(ResidentModel resident) {
                new androidx.appcompat.app.AlertDialog.Builder(MainStaffDashboardActivity.this)
                        .setTitle("Delete Resident")
                        .setMessage(
                                "Are you sure you want to delete " + resident.getName() + "? This cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            db.collection("users").document(resident.getId()).delete()
                                    .addOnSuccessListener(
                                            aVoid -> android.widget.Toast.makeText(MainStaffDashboardActivity.this,
                                                    "Resident deleted", android.widget.Toast.LENGTH_SHORT).show());
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    private void loadResidents() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                communityId = documentSnapshot.getString("communityId");
                communityName = documentSnapshot.getString("communityName");
                if (communityId != null) {
                    startResidentListener();
                } else {
                    android.widget.Toast.makeText(this, "Error: No community assigned to your account",
                            android.widget.Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void startResidentListener() {
        db.collection("communities").document(communityId)
                .collection("members")
                .whereEqualTo("role", RoleManager.ROLE_RESIDENT)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        return;
                    }

                    if (snapshots != null) {
                        staffResidentList.clear();
                        selfResidentList.clear();
                        allResidents.clear();
                        for (QueryDocumentSnapshot document : snapshots) {
                            ResidentModel resident = document.toObject(ResidentModel.class);
                            resident.setId(document.getId());

                            if (resident.getStatus() == null || "approved".equalsIgnoreCase(resident.getStatus())) {
                                allResidents.add(resident);
                                if (RoleManager.ROLE_STAFF.equalsIgnoreCase(resident.getCreatedBy())) {
                                    staffResidentList.add(resident);
                                } else {
                                    selfResidentList.add(resident);
                                }
                            }
                        }
                        staffAdapter.notifyDataSetChanged();
                        selfAdapter.notifyDataSetChanged();

                        // Schedule notifications ONLY for staff-created residents' medicines
                        int medicineCount = scheduleAllResidentNotifications(staffResidentList);
                        if (!staffResidentList.isEmpty()) {
                            android.widget.Toast.makeText(MainStaffDashboardActivity.this,
                                    "Synced " + medicineCount + " medicines for " + staffResidentList.size()
                                            + " staff-created residents",
                                    android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private int scheduleAllResidentNotifications(List<ResidentModel> residents) {
        int totalScheduled = 0;
        for (ResidentModel resident : residents) {
            if (resident.getMedicines() != null) {
                for (MedicineModel med : resident.getMedicines()) {
                    totalScheduled += scheduleMedicineNotification(med, resident.getId(), resident.getName());
                }
            }
            if (resident.getDoctorVisitDate() != null && !resident.getDoctorVisitDate().isEmpty() &&
                    resident.getDoctorVisitTime() != null && !resident.getDoctorVisitTime().isEmpty()) {
                scheduleDoctorVisitNotification(resident.getDoctorVisitDate(), resident.getDoctorVisitTime(),
                        resident.getId(), resident.getName());
            }
        }
        return totalScheduled;
    }

    private void scheduleDoctorVisitNotification(String date, String time, String residentId, String residentName) {
        try {
            String[] dateParts = date.split("/");
            if (dateParts.length < 3)
                return;
            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1;
            int year = Integer.parseInt(dateParts[2]);

            String[] timeParts = time.split(":");
            if (timeParts.length < 2)
                return;
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Calendar targetTime = Calendar.getInstance();
            targetTime.set(Calendar.YEAR, year);
            targetTime.set(Calendar.MONTH, month);
            targetTime.set(Calendar.DAY_OF_MONTH, day);
            targetTime.set(Calendar.HOUR_OF_DAY, hour);
            targetTime.set(Calendar.MINUTE, minute);
            targetTime.set(Calendar.SECOND, 0);

            // Staff reminders: 1 hour before and at the time
            scheduleSingleReminder(targetTime, -1, "Doctor visit in 1 hour: " + residentName, residentId,
                    "StaffDocVisit1");
            scheduleSingleReminder(targetTime, 0, "Doctor visit now: " + residentName, residentId, "StaffDocVisit0");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleSingleReminder(Calendar targetTime, int offsetHours, String message, String residentId,
            String requestCodeSuffix) {
        Calendar reminderTime = (Calendar) targetTime.clone();
        reminderTime.add(Calendar.HOUR_OF_DAY, offsetHours);

        if (reminderTime.getTimeInMillis() <= System.currentTimeMillis()) {
            return;
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("title", "Doctor Visit Reminder");
        intent.putExtra("message", message);
        intent.putExtra("residentId", residentId);

        int requestCode = (requestCodeSuffix + residentId).hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(),
                    pendingIntent);
        }
    }

    private int scheduleMedicineNotification(MedicineModel medicine, String residentId, String residentName) {
        int scheduledCount = 0;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (medicine.getExactTimes() != null) {
            for (String timeStr : medicine.getExactTimes()) {
                String[] parts = timeStr.split(":");
                if (parts.length == 2) {
                    int hour = Integer.parseInt(parts[0]);
                    int minute = Integer.parseInt(parts[1]);
                    String message = "Medicine: " + medicine.getMedicineName() + " for " + residentName;
                    scheduleAlarm(alarmManager, medicine.getMedicineName(), residentId, hour, minute, message);
                    scheduledCount++;
                }
            }
        }
        return scheduledCount;
    }

    private void scheduleAlarm(AlarmManager alarmManager, String medicineName, String residentId, int hour,
            int minute, String message) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("medicineName", medicineName);
        intent.putExtra("residentId", residentId);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);
        intent.putExtra("message", message);

        int requestCode = (residentId + medicineName + hour + minute).hashCode();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(),
                        pendingIntent);
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        pendingIntent);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_staff_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
