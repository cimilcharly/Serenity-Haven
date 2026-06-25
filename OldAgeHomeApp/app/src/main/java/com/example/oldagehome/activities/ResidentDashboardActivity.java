package com.example.oldagehome.activities;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.oldagehome.R;
import com.example.oldagehome.databinding.ActivityResidentDashboardBinding;
import com.example.oldagehome.models.UserModel;
import com.example.oldagehome.utils.AlarmReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ResidentDashboardActivity extends AppCompatActivity {

    private ActivityResidentDashboardBinding binding;
    private FirebaseFirestore db;
    private String uid;
    private String userEmail = "";
    private String communityId = null;
    private String communityName = null;
    private String userName = "";
    private int userAge = 0;
    private String joinCode = "";
    private String profileImageUrl = "";
    private String userRole = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResidentDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        db = FirebaseFirestore.getInstance();

        // Initial setup: View Mode (Disabled)
        toggleEditMode(false);
        binding.btnSaveDetails.setText("Edit Details");
        binding.btnSaveDetails.setBackgroundResource(R.drawable.bg_btn_red); // Red for Edit

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            loadUserProfile();
        }

        binding.btnSaveDetails.setOnClickListener(v -> {
            String btnText = binding.btnSaveDetails.getText().toString();
            if (btnText.equals("Edit Details")) {
                toggleEditMode(true);
                binding.btnSaveDetails.setText("Save Details");
                binding.btnSaveDetails.setBackgroundResource(R.drawable.bg_btn_green); // Green for Save
            } else {
                saveUserProfile();
            }
        });

        binding.etDoctorVisitDate.setOnClickListener(v -> showDatePicker());
        binding.etDoctorVisitTime.setOnClickListener(v -> showTimePicker());

        binding.cardMedicines.setOnClickListener(v -> {
            Intent intent = new Intent(this, MedicineActivity.class);
            intent.putExtra("residentId", uid);
            startActivity(intent);
        });

        binding.cardChatbot.setOnClickListener(v -> {
            startActivity(new Intent(this, ChatbotActivity.class));
        });

        // Load User Profile Picture if available (e.g. from Google Account)
        com.google.firebase.auth.FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getPhotoUrl() != null) {
            com.bumptech.glide.Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.ic_avatar_placeholder)
                    .error(R.drawable.ic_avatar_placeholder)
                    .into(binding.ivProfileAvatar);
        }

        binding.ivCart.setOnClickListener(v -> {
            startActivity(new Intent(this, MedicineStoreActivity.class));
        });

        binding.cardProfile.setOnClickListener(v -> {
            showUserProfileDialog();
        });

        binding.tvContactStaff.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(android.net.Uri.parse("mailto:arunjohny2412@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Need Help - Resident " + binding.etName.getText().toString());
            try {
                startActivity(intent);
            } catch (android.content.ActivityNotFoundException e) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    String date = String.format("%02d/%02d/%d", dayOfMonth, month1 + 1, year1);
                    binding.etDoctorVisitDate.setText(date);
                }, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Prevent past dates
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minuteOfHour) -> {
                    String time = String.format("%02d:%02d", hourOfDay, minuteOfHour);
                    binding.etDoctorVisitTime.setText(time);
                }, hour, minute, true);
        timePickerDialog.show();
    }

    private void toggleEditMode(boolean enable) {
        binding.etName.setEnabled(enable);
        binding.etAge.setEnabled(enable);
        binding.etRoomNumber.setEnabled(enable);
        binding.etMedicalCondition.setEnabled(enable);
        binding.etDoctorName.setEnabled(enable);
        binding.etDoctorVisitDate.setEnabled(enable);
        binding.etDoctorVisitDate.setClickable(enable);
        binding.etDoctorVisitTime.setEnabled(enable);
        binding.etDoctorVisitTime.setClickable(enable);
    }

    private void loadUserProfile() {
        if (uid == null)
            return;
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            userName = user.getName() != null ? user.getName() : "";
                            userEmail = user.getEmail() != null ? user.getEmail() : "";
                            userAge = user.getAge();
                            profileImageUrl = user.getProfileImageUrl() != null ? user.getProfileImageUrl() : "";
                            userRole = user.getRole() != null ? user.getRole() : "";
                            communityId = user.getCommunityId();
                            communityName = user.getCommunityName();

                            // Load user photo into toolbar avatar if available
                            com.google.firebase.auth.FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String avatarUrl = (!profileImageUrl.isEmpty()) ? profileImageUrl : (currentUser != null && currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null);
                            com.bumptech.glide.Glide.with(ResidentDashboardActivity.this)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.ic_avatar_placeholder)
                                    .error(R.drawable.ic_avatar_placeholder)
                                    .into(binding.ivProfileAvatar);

                            String welcomeText = "Welcome "
                                    + (!userName.isEmpty() ? userName : "Home");
                            binding.tvWelcome.setText(welcomeText);

                            boolean isMe = com.example.oldagehome.utils.RoleManager.ROLE_ME.equals(userRole);
                            if (isMe) {
                                binding.tvCommunityName.setText("Personal Space");
                                binding.tilRoomNumber.setVisibility(View.GONE);
                                android.widget.LinearLayout.LayoutParams params =
                                        (android.widget.LinearLayout.LayoutParams) binding.tilAge.getLayoutParams();
                                params.rightMargin = 0;
                                params.setMarginEnd(0);
                                binding.tilAge.setLayoutParams(params);
                            } else {
                                binding.tilRoomNumber.setVisibility(View.VISIBLE);
                                android.widget.LinearLayout.LayoutParams params =
                                        (android.widget.LinearLayout.LayoutParams) binding.tilAge.getLayoutParams();
                                int marginInPx = (int) (8 * getResources().getDisplayMetrics().density);
                                params.rightMargin = marginInPx;
                                params.setMarginEnd(marginInPx);
                                binding.tilAge.setLayoutParams(params);

                                if (communityName != null && !communityName.isEmpty()) {
                                    binding.tvCommunityName.setText(communityName);
                                } else {
                                    binding.tvCommunityName.setText("Hello, Resident");
                                }
                            }
                            binding.etName.setText(userName);
                            binding.etAge.setText(userAge > 0 ? String.valueOf(userAge) : "");
                            binding.etRoomNumber.setText(user.getRoomNumber());
                            binding.etMedicalCondition.setText(user.getMedicalHistory());
                            binding.etDoctorName.setText(user.getDoctorName() != null ? user.getDoctorName() : "");
                            binding.etDoctorVisitDate.setText(user.getDoctorVisitDate());
                            binding.etDoctorVisitTime.setText(user.getDoctorVisitTime());

                            // If communityId is present, fetch community details to get joinCode
                            if (communityId != null) {
                                db.collection("communities").document(communityId).get()
                                        .addOnSuccessListener(commDoc -> {
                                            if (commDoc.exists()) {
                                                joinCode = commDoc.getString("joinCode");
                                                if (joinCode == null) joinCode = "";
                                                if (!joinCode.isEmpty()) {
                                                    binding.tvJoinCode.setText("Join Code: " + joinCode);
                                                    binding.tvJoinCode.setVisibility(View.VISIBLE);
                                                } else {
                                                    binding.tvJoinCode.setVisibility(View.GONE);
                                                }
                                            }
                                        });
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show());
    }

    private void saveUserProfile() {
        if (uid == null)
            return;

        binding.btnSaveDetails.setEnabled(false); // Prevent double click

        saveProfileData();
    }

    private void saveProfileData() {
        String name = binding.etName.getText().toString().trim();
        String ageStr = binding.etAge.getText().toString().trim();
        String roomNumber = binding.etRoomNumber.getText().toString().trim();
        String medicalCondition = binding.etMedicalCondition.getText().toString().trim();
        String doctorName = binding.etDoctorName.getText().toString().trim();
        String doctorVisitDate = binding.etDoctorVisitDate.getText().toString().trim();
        String doctorVisitTime = binding.etDoctorVisitTime.getText().toString().trim();

        if (name.isEmpty()) {
            binding.etName.setError("Name is required");
            binding.btnSaveDetails.setEnabled(true);
            return;
        }

        int tempAge = 0;
        if (!ageStr.isEmpty()) {
            try {
                tempAge = Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                binding.etAge.setError("Invalid age");
                binding.btnSaveDetails.setEnabled(true);
                return;
            }
        }
        final int finalAge = tempAge;

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("age", finalAge);
        if (!com.example.oldagehome.utils.RoleManager.ROLE_ME.equals(userRole)) {
            updates.put("roomNumber", roomNumber);
        }
        updates.put("medicalHistory", medicalCondition);
        updates.put("doctorName", doctorName);
        updates.put("doctorVisitDate", doctorVisitDate);
        updates.put("doctorVisitTime", doctorVisitTime);

        db.collection("users").document(uid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (communityId != null) {
                        db.collection("communities").document(communityId)
                                .collection("members").document(uid).update(updates);
                    }
                    userName = name;
                    userAge = finalAge;
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    binding.tvWelcome.setText("Welcome " + name);

                    if (!doctorVisitDate.isEmpty() && !doctorVisitTime.isEmpty()) {
                        scheduleDoctorVisitNotification(doctorVisitDate, doctorVisitTime);
                    }

                    // Switch back to view mode
                    toggleEditMode(false);
                    binding.btnSaveDetails.setText("Edit Details");
                    binding.btnSaveDetails.setBackgroundResource(R.drawable.bg_btn_red);
                    binding.btnSaveDetails.setEnabled(true);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                    binding.btnSaveDetails.setEnabled(true);
                });
    }

    private void scheduleDoctorVisitNotification(String date, String time) {
        try {
            String[] dateParts = date.split("/");
            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1; // Month is 0-indexed
            int year = Integer.parseInt(dateParts[2]);

            String[] timeParts = time.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            java.util.Calendar targetTime = java.util.Calendar.getInstance();
            targetTime.set(Calendar.YEAR, year);
            targetTime.set(Calendar.MONTH, month);
            targetTime.set(Calendar.DAY_OF_MONTH, day);
            targetTime.set(Calendar.HOUR_OF_DAY, hour);
            targetTime.set(Calendar.MINUTE, minute);
            targetTime.set(Calendar.SECOND, 0);

            // Schedule multiple reminders
            scheduleSingleReminder(targetTime, -24, "Doctor visit in 24 hours", "DoctorVisit24");
            scheduleSingleReminder(targetTime, -12, "Doctor visit in 12 hours", "DoctorVisit12");
            scheduleSingleReminder(targetTime, -5, "Doctor visit in 5 hours", "DoctorVisit5");
            scheduleSingleReminder(targetTime, 0, "Doctor visit is starting now", "DoctorVisit0");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void scheduleSingleReminder(java.util.Calendar targetTime, int offsetHours, String messagePrefix,
            String requestCodeSuffix) {
        java.util.Calendar reminderTime = (java.util.Calendar) targetTime.clone();
        reminderTime.add(java.util.Calendar.HOUR_OF_DAY, offsetHours);

        if (reminderTime.getTimeInMillis() <= System.currentTimeMillis()) {
            return;
        }

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("title", "Doctor Visit Reminder");
        intent.putExtra("message", messagePrefix + " at " + String.format("%02d:%02d",
                targetTime.get(java.util.Calendar.HOUR_OF_DAY), targetTime.get(java.util.Calendar.MINUTE)));
        intent.putExtra("residentId", uid);
        intent.putExtra("userEmail", userEmail);

        // Use a stable requestCode based on UID and Suffix
        int requestCode = (requestCodeSuffix + uid).hashCode(); // Unique per user and per alarm type
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime.getTimeInMillis(),
                    pendingIntent);
        }
    }

    private void showUserProfileDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user_profile, null);
        
        android.widget.ImageView btnClose = dialogView.findViewById(R.id.btnDialogClose);
        android.widget.ImageView ivAvatar = dialogView.findViewById(R.id.ivDialogAvatar);
        android.widget.TextView tvName = dialogView.findViewById(R.id.tvDialogName);
        android.widget.TextView tvRole = dialogView.findViewById(R.id.tvDialogRole);
        android.widget.TextView tvEmail = dialogView.findViewById(R.id.tvDialogEmail);
        
        android.widget.LinearLayout layoutAge = dialogView.findViewById(R.id.layoutDialogAge);
        android.widget.TextView tvAge = dialogView.findViewById(R.id.tvDialogAge);
        android.view.View dividerAge = dialogView.findViewById(R.id.dividerDialogAge);
        
        android.widget.LinearLayout layoutCommunity = dialogView.findViewById(R.id.layoutDialogCommunity);
        android.widget.TextView tvCommunityName = dialogView.findViewById(R.id.tvDialogCommunityName);
        android.view.View dividerCommunity = dialogView.findViewById(R.id.dividerDialogCommunity);
        
        android.widget.LinearLayout layoutCode = dialogView.findViewById(R.id.layoutDialogCode);
        android.widget.TextView tvCommunityCode = dialogView.findViewById(R.id.tvDialogCommunityCode);
        android.widget.ImageView btnCopy = dialogView.findViewById(R.id.btnCopyCode);
        
        // Navigation Tabs & Toggles
        android.widget.LinearLayout btnTabPersonal = dialogView.findViewById(R.id.btnTabPersonal);
        android.widget.LinearLayout btnTabNotifications = dialogView.findViewById(R.id.btnTabNotifications);
        android.widget.LinearLayout btnTabAbout = dialogView.findViewById(R.id.btnTabAbout);
        
        android.widget.TextView tvTabPersonalText = dialogView.findViewById(R.id.tvTabPersonalText);
        android.widget.TextView tvTabNotificationsText = dialogView.findViewById(R.id.tvTabNotificationsText);
        android.widget.TextView tvTabAboutText = dialogView.findViewById(R.id.tvTabAboutText);
        
        android.view.View indicatorPersonal = dialogView.findViewById(R.id.indicatorPersonal);
        android.view.View indicatorNotifications = dialogView.findViewById(R.id.indicatorNotifications);
        android.view.View indicatorAbout = dialogView.findViewById(R.id.indicatorAbout);
        
        android.widget.LinearLayout layoutSectionPersonal = dialogView.findViewById(R.id.layoutSectionPersonal);
        android.widget.LinearLayout layoutSectionNotifications = dialogView.findViewById(R.id.layoutSectionNotifications);
        android.widget.LinearLayout layoutSectionAbout = dialogView.findViewById(R.id.layoutSectionAbout);
        
        androidx.appcompat.widget.SwitchCompat switchNotifications = dialogView.findViewById(R.id.switchNotifications);
        androidx.appcompat.widget.AppCompatButton btnLogout = dialogView.findViewById(R.id.btnDialogLogout);
        
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Basic Profile Info
        tvName.setText(userName != null && !userName.isEmpty() ? userName : "Resident Member");
        
        boolean isMeRole = com.example.oldagehome.utils.RoleManager.ROLE_ME.equals(userRole);
        tvRole.setText(isMeRole ? "Personal" : "Resident");
        tvEmail.setText(userEmail != null && !userEmail.isEmpty() ? userEmail : "No Email");

        // Age: only display if > 0
        if (userAge > 0) {
            tvAge.setText(String.valueOf(userAge));
            layoutAge.setVisibility(View.VISIBLE);
            dividerAge.setVisibility(View.VISIBLE);
        } else {
            layoutAge.setVisibility(View.GONE);
            dividerAge.setVisibility(View.GONE);
        }

        // Community Details
        if (isMeRole) {
            tvCommunityName.setText("Personal Space");
            layoutCommunity.setVisibility(View.VISIBLE);
            dividerCommunity.setVisibility(View.GONE);
            layoutCode.setVisibility(View.GONE);
        } else {
            if (communityName != null && !communityName.isEmpty()) {
                tvCommunityName.setText(communityName);
                layoutCommunity.setVisibility(View.VISIBLE);
                dividerCommunity.setVisibility(View.VISIBLE);
            } else {
                layoutCommunity.setVisibility(View.GONE);
                dividerCommunity.setVisibility(View.GONE);
            }

            if (joinCode != null && !joinCode.isEmpty()) {
                tvCommunityCode.setText(joinCode);
                layoutCode.setVisibility(View.VISIBLE);
                
                btnCopy.setOnClickListener(v -> {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("Community Join Code", joinCode);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(this, "Join Code copied to clipboard", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                layoutCode.setVisibility(View.GONE);
            }
        }

        // Setup switch preferences
        android.content.SharedPreferences prefs = getSharedPreferences("app_settings", MODE_PRIVATE);
        switchNotifications.setChecked(prefs.getBoolean("notifications_enabled", true));
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
            if (isChecked) {
                Toast.makeText(this, "Reminders enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Reminders silenced", Toast.LENGTH_SHORT).show();
            }
        });

        // Tab selection logic
        btnTabPersonal.setOnClickListener(v -> {
            tvTabPersonalText.setTextColor(getResources().getColor(R.color.brand_start));
            tvTabPersonalText.setTypeface(null, android.graphics.Typeface.BOLD);
            indicatorPersonal.setBackgroundColor(getResources().getColor(R.color.brand_start));

            tvTabNotificationsText.setTextColor(getResources().getColor(R.color.text_grey));
            tvTabNotificationsText.setTypeface(null, android.graphics.Typeface.NORMAL);
            indicatorNotifications.setBackgroundColor(android.graphics.Color.TRANSPARENT);

            tvTabAboutText.setTextColor(getResources().getColor(R.color.text_grey));
            tvTabAboutText.setTypeface(null, android.graphics.Typeface.NORMAL);
            indicatorAbout.setBackgroundColor(android.graphics.Color.TRANSPARENT);

            layoutSectionPersonal.setVisibility(View.VISIBLE);
            layoutSectionNotifications.setVisibility(View.GONE);
            layoutSectionAbout.setVisibility(View.GONE);
        });

        btnTabNotifications.setOnClickListener(v -> {
            tvTabPersonalText.setTextColor(getResources().getColor(R.color.text_grey));
            tvTabPersonalText.setTypeface(null, android.graphics.Typeface.NORMAL);
            indicatorPersonal.setBackgroundColor(android.graphics.Color.TRANSPARENT);

            tvTabNotificationsText.setTextColor(getResources().getColor(R.color.brand_start));
            tvTabNotificationsText.setTypeface(null, android.graphics.Typeface.BOLD);
            indicatorNotifications.setBackgroundColor(getResources().getColor(R.color.brand_start));

            tvTabAboutText.setTextColor(getResources().getColor(R.color.text_grey));
            tvTabAboutText.setTypeface(null, android.graphics.Typeface.NORMAL);
            indicatorAbout.setBackgroundColor(android.graphics.Color.TRANSPARENT);

            layoutSectionPersonal.setVisibility(View.GONE);
            layoutSectionNotifications.setVisibility(View.VISIBLE);
            layoutSectionAbout.setVisibility(View.GONE);
        });

        btnTabAbout.setOnClickListener(v -> {
            tvTabPersonalText.setTextColor(getResources().getColor(R.color.text_grey));
            tvTabPersonalText.setTypeface(null, android.graphics.Typeface.NORMAL);
            indicatorPersonal.setBackgroundColor(android.graphics.Color.TRANSPARENT);

            tvTabNotificationsText.setTextColor(getResources().getColor(R.color.text_grey));
            tvTabNotificationsText.setTypeface(null, android.graphics.Typeface.NORMAL);
            indicatorNotifications.setBackgroundColor(android.graphics.Color.TRANSPARENT);

            tvTabAboutText.setTextColor(getResources().getColor(R.color.brand_start));
            tvTabAboutText.setTypeface(null, android.graphics.Typeface.BOLD);
            indicatorAbout.setBackgroundColor(getResources().getColor(R.color.brand_start));

            layoutSectionPersonal.setVisibility(View.GONE);
            layoutSectionNotifications.setVisibility(View.GONE);
            layoutSectionAbout.setVisibility(View.VISIBLE);
        });

        // Load Avatar URL
        com.google.firebase.auth.FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String avatarUrl = (profileImageUrl != null && !profileImageUrl.isEmpty()) ? profileImageUrl : (currentUser != null && currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : null);
        com.bumptech.glide.Glide.with(this)
                .load(avatarUrl)
                .placeholder(R.drawable.ic_avatar_placeholder)
                .error(R.drawable.ic_avatar_placeholder)
                .into(ivAvatar);

        btnLogout.setOnClickListener(v -> {
            dialog.dismiss();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_resident_dashboard, menu);
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
