package com.example.oldagehome.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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

        binding.cardInvite.setOnClickListener(v -> showInviteDialog());

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
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        communityId = documentSnapshot.getString("communityId");
                        communityName = documentSnapshot.getString("communityName");
                        
                        if (communityId != null) {
                            // Always fetch the community details to get the Join Code and ensure the name is correct
                            db.collection("communities").document(communityId).get()
                                    .addOnSuccessListener(commDoc -> {
                                        if (commDoc.exists()) {
                                            String fetchedName = commDoc.getString("name");
                                            String joinCode = commDoc.getString("joinCode");
                                            
                                            if (fetchedName != null && !fetchedName.isEmpty()) {
                                                communityName = fetchedName;
                                                // Sync user profile name if null in the user doc
                                                if (documentSnapshot.getString("communityName") == null) {
                                                    db.collection("users").document(uid).update("communityName", communityName);
                                                }
                                            }
                                            
                                            // Update header UI
                                            binding.tvCommunityName.setText(communityName);
                                            if (joinCode != null && !joinCode.isEmpty()) {
                                                binding.tvJoinCode.setText("Join Code: " + joinCode);
                                                binding.tvJoinCode.setVisibility(View.VISIBLE);
                                            } else {
                                                binding.tvJoinCode.setVisibility(View.GONE);
                                            }
                                        } else {
                                            binding.tvCommunityName.setText(communityName != null ? communityName : "My Community");
                                            binding.tvJoinCode.setVisibility(View.GONE);
                                        }
                                        startResidentListener();
                                    })
                                    .addOnFailureListener(e -> {
                                        binding.tvCommunityName.setText(communityName != null ? communityName : "My Community");
                                        binding.tvJoinCode.setVisibility(View.GONE);
                                        startResidentListener();
                                    });
                        } else {
                            // Try to look up community created by this user
                            db.collection("communities").whereEqualTo("creatorUid", uid).get()
                                    .addOnSuccessListener(querySnapshot -> {
                                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                            com.google.firebase.firestore.DocumentSnapshot commDoc = querySnapshot.getDocuments().get(0);
                                            communityId = commDoc.getId();
                                            communityName = commDoc.getString("name");
                                            String joinCode = commDoc.getString("joinCode");
                                            
                                            if (communityId != null) {
                                                java.util.Map<String, Object> updates = new java.util.HashMap<>();
                                                updates.put("communityId", communityId);
                                                if (communityName != null) {
                                                    updates.put("communityName", communityName);
                                                }
                                                db.collection("users").document(uid).update(updates);
                                                
                                                // Update UI
                                                binding.tvCommunityName.setText(communityName != null ? communityName : "My Community");
                                                if (joinCode != null && !joinCode.isEmpty()) {
                                                    binding.tvJoinCode.setText("Join Code: " + joinCode);
                                                    binding.tvJoinCode.setVisibility(View.VISIBLE);
                                                } else {
                                                    binding.tvJoinCode.setVisibility(View.GONE);
                                                }
                                                
                                                startResidentListener();
                                                return;
                                            }
                                        }
                                        binding.tvCommunityName.setText("No Community Assigned");
                                        binding.tvJoinCode.setVisibility(View.GONE);
                                        android.widget.Toast.makeText(this, "Error: No community assigned to your account",
                                                android.widget.Toast.LENGTH_LONG).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        binding.tvCommunityName.setText("No Community Assigned");
                                        binding.tvJoinCode.setVisibility(View.GONE);
                                        android.widget.Toast.makeText(this, "Error: No community assigned to your account",
                                                android.widget.Toast.LENGTH_LONG).show();
                                    });
                        }
                    } else {
                        android.widget.Toast.makeText(this, "Error: User profile not found in database",
                                android.widget.Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    android.widget.Toast.makeText(this, "Failed to load community details: " + e.getMessage(),
                            android.widget.Toast.LENGTH_LONG).show();
                });
    }

    private void showInviteDialog() {
        if (communityId == null || communityName == null) {
            android.widget.Toast.makeText(this, "Please wait, loading community details...", android.widget.Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_invite_member, null);
        com.google.android.material.textfield.TextInputEditText etEmail = dialogView.findViewById(R.id.etInviteEmail);
        com.google.android.material.textfield.TextInputEditText etNote = dialogView.findViewById(R.id.etInviteNote);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Send Email", (dialog, which) -> {
                    String recipientEmail = etEmail.getText().toString().trim();
                    String note = etNote.getText().toString().trim();

                    if (recipientEmail.isEmpty()) {
                        android.widget.Toast.makeText(this, "Email is required", android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }

                    sendInvitationEmail(recipientEmail, note);
                })
                .setNeutralButton("Share Link", (dialog, which) -> {
                    String note = etNote.getText().toString().trim();
                    shareInvitationLink(note);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void shareInvitationLink(String note) {
        String encodedName = android.net.Uri.encode(communityName);
        String inviteLink = "https://cimilcharly.github.io/Serenity-Haven/join.html?communityId=" + communityId + "&communityName=" + encodedName;

        StringBuilder shareBody = new StringBuilder();
        shareBody.append("You have been invited to join the community \"").append(communityName).append("\" on Serenity Haven!\n\n");
        if (note != null && !note.isEmpty()) {
            shareBody.append("Personal Note from Staff:\n");
            shareBody.append("\"").append(note).append("\"\n\n");
        }
        shareBody.append("To join directly and set up your account, click the link below on your Android device:\n");
        shareBody.append(inviteLink).append("\n\n");
        shareBody.append("Best regards,\nSerenity Haven Team");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Join " + communityName + " on Serenity Haven");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody.toString());

        startActivity(Intent.createChooser(shareIntent, "Share Invitation Link via"));
    }

    private void sendInvitationEmail(String recipientEmail, String note) {
        String encodedName = android.net.Uri.encode(communityName);
        String inviteLink = "https://cimilcharly.github.io/Serenity-Haven/join.html?communityId=" + communityId + "&communityName=" + encodedName;

        String subject = "Invitation to join " + communityName + " on Serenity Haven";
        StringBuilder body = new StringBuilder();
        body.append("<html>");
        body.append("<body style='font-family: Arial, sans-serif; color: #333333; line-height: 1.6;'>");
        body.append("<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 8px;'>");
        body.append("<h2 style='color: #00BCD4;'>Serenity Haven Invitation</h2>");
        body.append("<p>Hello,</p>");
        body.append("<p>You have been invited by the Main Staff to join the community \"<strong>").append(communityName).append("</strong>\" on the Serenity Haven application.</p>");
        if (!note.isEmpty()) {
            body.append("<div style='background-color: #f9f9f9; border-left: 4px solid #00BCD4; padding: 12px; margin: 16px 0;'>");
            body.append("<strong>Personal Note from Staff:</strong><br/>");
            body.append("\"").append(note).append("\"");
            body.append("</div>");
        }
        body.append("<p>To join directly and set up your account, click the button below from your Android device:</p>");
        body.append("<p style='text-align: center; margin: 24px 0;'>");
        body.append("<a href='").append(inviteLink).append("' style='background-color: #00BCD4; color: white; padding: 12px 24px; text-decoration: none; border-radius: 20px; font-weight: bold; display: inline-block; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>Open in Serenity Haven App</a>");
        body.append("</p>");
        body.append("<p style='font-size: 12px; color: #666666;'>If the button above is not clickable, copy and paste this link in your mobile browser or click it to launch the app directly: <br/>");
        body.append("<a href='").append(inviteLink).append("'>").append(inviteLink).append("</a></p>");
        body.append("<hr style='border: none; border-top: 1px solid #e0e0e0; margin: 20px 0;'/>");
        body.append("<p style='font-size: 12px; color: #999999;'>Best regards,<br/>Serenity Haven Team</p>");
        body.append("</div>");
        body.append("</body>");
        body.append("</html>");

        // SMTP Credentials
        final String senderEmail = "arunjohny2412@gmail.com";
        final String senderPassword = "kkwq uoot ausj yrvf";

        new Thread(() -> {
            com.example.oldagehome.utils.EmailSender.sendEmail(senderEmail, senderPassword, recipientEmail, subject, body.toString());
            runOnUiThread(() -> {
                android.widget.Toast.makeText(this, "Invitation sent to " + recipientEmail, android.widget.Toast.LENGTH_LONG).show();
            });
        }).start();
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
