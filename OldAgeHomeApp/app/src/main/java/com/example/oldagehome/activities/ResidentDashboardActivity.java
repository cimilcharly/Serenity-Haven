package com.example.oldagehome.activities;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
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
    private String userEmail;
    private String communityId;
    private String communityName;

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

        binding.tvLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
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
                            userEmail = user.getEmail(); // Store email
                            communityId = user.getCommunityId();
                            communityName = user.getCommunityName();
                            String welcomeText = "Welcome "
                                    + (user.getName() != null && !user.getName().isEmpty() ? user.getName() : "Home");
                            binding.tvWelcome.setText(welcomeText);
                            binding.etName.setText(user.getName());
                            binding.etAge.setText(user.getAge() > 0 ? String.valueOf(user.getAge()) : "");
                            binding.etRoomNumber.setText(user.getRoomNumber());
                            binding.etMedicalCondition.setText(user.getMedicalHistory());
                            binding.etDoctorName.setText(user.getDoctorName() != null ? user.getDoctorName() : "");
                            binding.etDoctorVisitDate.setText(user.getDoctorVisitDate());
                            binding.etDoctorVisitTime.setText(user.getDoctorVisitTime());
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

        int age = 0;
        if (!ageStr.isEmpty()) {
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                binding.etAge.setError("Invalid age");
                binding.btnSaveDetails.setEnabled(true);
                return;
            }
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("age", age);
        updates.put("roomNumber", roomNumber);
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
