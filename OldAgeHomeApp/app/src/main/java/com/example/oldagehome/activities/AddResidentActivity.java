package com.example.oldagehome.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.oldagehome.R;
import com.example.oldagehome.databinding.ActivityAddResidentBinding;
import com.example.oldagehome.models.UserModel;
import com.example.oldagehome.utils.RoleManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import java.util.Calendar;

public class AddResidentActivity extends AppCompatActivity {

    private ActivityAddResidentBinding binding;
    private FirebaseFirestore db;
    private String residentId = null; // Null means Adding, Value means Editing
    private String communityName = null;
    private String communityId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddResidentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();

        // Check if editing
        if (getIntent().hasExtra("residentId")) {
            residentId = getIntent().getStringExtra("residentId");
            loadResidentData();
            binding.btnSave.setText("Update Resident");
        }
        if (getIntent().hasExtra("communityName")) {
            communityName = getIntent().getStringExtra("communityName");
        }
        if (getIntent().hasExtra("communityId")) {
            communityId = getIntent().getStringExtra("communityId");
        }

        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> prepareSave());

        binding.etDoctorVisitDate.setOnClickListener(v -> showDatePicker());
        binding.etDoctorVisitTime.setOnClickListener(v -> showTimePicker());
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
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
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

    private void loadResidentData() {
        if (residentId == null)
            return;
        db.collection("users").document(residentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        if (user != null) {
                            binding.etName.setText(user.getName());
                            binding.etAge.setText(user.getAge() > 0 ? String.valueOf(user.getAge()) : "");
                            binding.etGender.setText(user.getGender());
                            binding.etRoom.setText(user.getRoomNumber());
                            binding.etMedical.setText(user.getMedicalHistory());
                            binding.etEmergency.setText(user.getEmergencyContact());
                            binding.etDoctorName.setText(user.getDoctorName() != null ? user.getDoctorName() : "");
                            binding.etDoctorVisitDate
                                    .setText(user.getDoctorVisitDate() != null ? user.getDoctorVisitDate() : "");
                            binding.etDoctorVisitTime
                                    .setText(user.getDoctorVisitTime() != null ? user.getDoctorVisitTime() : "");
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show());
    }

    private void prepareSave() {
        saveResident();
    }

    private void saveResident() {
        String name = binding.etName.getText().toString().trim();
        String ageStr = binding.etAge.getText().toString().trim();
        String gender = binding.etGender.getText().toString().trim();
        String room = binding.etRoom.getText().toString().trim();
        String medical = binding.etMedical.getText().toString().trim();
        String emergency = binding.etEmergency.getText().toString().trim();
        String doctorName = binding.etDoctorName.getText().toString().trim();
        String visitDate = binding.etDoctorVisitDate.getText().toString().trim();
        String visitTime = binding.etDoctorVisitTime.getText().toString().trim();

        if (name.isEmpty() || ageStr.isEmpty()) {
            Toast.makeText(this, "Name and Age are required", Toast.LENGTH_SHORT).show();
            return;
        }

        int age = Integer.parseInt(ageStr);

        // If Editing
        if (residentId != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("name", name);
            updates.put("age", age);
            updates.put("gender", gender);
            updates.put("roomNumber", room);
            updates.put("medicalHistory", medical);
            updates.put("emergencyContact", emergency);
            updates.put("doctorName", doctorName);
            updates.put("doctorVisitDate", visitDate);
            updates.put("doctorVisitTime", visitTime);

            db.collection("users").document(residentId).update(updates);

            db.collection("communities").document(communityId)
                    .collection("members").document(residentId).update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Resident Updated", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(
                            e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        } else {
            // Adding New
            String uid = db.collection("users").document().getId();
            UserModel user = new UserModel();
            user.setUid(uid);
            user.setName(name);
            user.setAge(age);
            user.setGender(gender);
            user.setRoomNumber(room);
            user.setMedicalHistory(medical);
            user.setEmergencyContact(emergency);
            user.setDoctorName(doctorName);
            user.setDoctorVisitDate(visitDate);
            user.setDoctorVisitTime(visitTime);
            user.setCommunityName(communityName);
            user.setCommunityId(communityId);
            user.setRole(RoleManager.ROLE_RESIDENT);
            user.setStatus("approved");
            user.setCreatedBy(RoleManager.ROLE_STAFF);
            user.setAdmissionDate(System.currentTimeMillis());
            user.setMedicines(new ArrayList<>());

            // Create matching global user doc for ResidentDashboardActivity queries
            Map<String, Object> globalUser = new HashMap<>();
            globalUser.put("uid", uid);
            globalUser.put("email", ""); // Caregiver-created resident does not log in via email
            globalUser.put("role", RoleManager.ROLE_RESIDENT);
            globalUser.put("communityName", communityName);
            globalUser.put("communityId", communityId);
            globalUser.put("status", "approved");
            globalUser.put("name", name);
            globalUser.put("age", age);
            globalUser.put("gender", gender);
            globalUser.put("roomNumber", room);
            globalUser.put("medicalHistory", medical);
            globalUser.put("emergencyContact", emergency);
            globalUser.put("doctorName", doctorName);
            globalUser.put("doctorVisitDate", visitDate);
            globalUser.put("doctorVisitTime", visitTime);
            globalUser.put("admissionDate", user.getAdmissionDate());
            globalUser.put("medicines", user.getMedicines());
            globalUser.put("createdBy", RoleManager.ROLE_STAFF);

            db.collection("users").document(uid).set(globalUser);

            db.collection("communities").document(communityId)
                    .collection("members").document(uid).set(user)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Resident Added to Community Database", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(
                            e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
