package com.example.oldagehome.activities;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.oldagehome.R;
import com.example.oldagehome.adapters.MedicineAdapter;
import com.example.oldagehome.databinding.ActivityMedicineBinding;
import com.example.oldagehome.models.MedicineModel;
import com.example.oldagehome.models.ResidentModel;
import com.example.oldagehome.utils.AlarmReceiver;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MedicineActivity extends AppCompatActivity {

    private ActivityMedicineBinding binding;
    private FirebaseFirestore db;
    private MedicineAdapter adapter;
    private List<MedicineModel> medicineList;
    private String residentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMedicineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        medicineList = new ArrayList<>();
        residentId = getIntent().getStringExtra("residentId");
        if (residentId == null) {
            Toast.makeText(this, "Error: Resident ID missing!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        binding.rvMedicines.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicineAdapter(medicineList);
        binding.rvMedicines.setAdapter(adapter);

        // Handle Custom Back Button
        binding.btnBack.setOnClickListener(v -> onBackPressed());

        // Set Adapter Listener
        adapter.setOnMedicineActionListener(new MedicineAdapter.OnMedicineActionListener() {
            @Override
            public void onEdit(MedicineModel medicine) {
                showAddMedicineDialog(medicine);
            }

            @Override
            public void onDelete(MedicineModel medicine) {
                deleteMedicine(medicine);
            }
        });

        fetchMedicines();

        binding.btnAddMedicine.setOnClickListener(v -> showAddMedicineDialog(null));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void deleteMedicine(MedicineModel medicine) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Medicine")
                .setMessage("Are you sure you want to delete " + medicine.getMedicineName() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    db.collection("users").document(residentId)
                            .update("medicines", FieldValue.arrayRemove(medicine))
                            .addOnSuccessListener(
                                    aVoid -> Toast.makeText(this, "Medicine deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(
                                    e -> Toast.makeText(this, "Error deleting", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void fetchMedicines() {
        if (residentId != null) {
            db.collection("users").document(residentId)
                    .addSnapshotListener((documentSnapshot, e) -> {
                        if (e != null) {
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            ResidentModel resident = documentSnapshot.toObject(ResidentModel.class);
                            if (resident != null && resident.getMedicines() != null
                                    && !resident.getMedicines().isEmpty()) {
                                medicineList.clear();
                                medicineList.addAll(resident.getMedicines());
                                adapter.notifyDataSetChanged();
                                binding.tvNoMedicines.setVisibility(View.GONE);
                                binding.rvMedicines.setVisibility(View.VISIBLE);

                                // Schedule alarms for fetched medicines
                                for (MedicineModel med : medicineList) {
                                    scheduleMedicineNotification(med);
                                }
                            } else {
                                medicineList.clear();
                                adapter.notifyDataSetChanged();
                                binding.tvNoMedicines.setVisibility(View.VISIBLE);
                                binding.rvMedicines.setVisibility(View.GONE);
                            }
                        }
                    });
        }
    }

    private void showAddMedicineDialog(MedicineModel medicineToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_medicine, null);
        builder.setView(dialogView);

        EditText etMedicineName = dialogView.findViewById(R.id.etMedicineName);
        EditText etDosage = dialogView.findViewById(R.id.etDosage);
        EditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        LinearLayout timeSlotsContainer = dialogView.findViewById(R.id.timeSlotsContainer);
        Button btnAddTime = dialogView.findViewById(R.id.btnAddTime);

        List<String> selectedTimes = new ArrayList<>();

        if (medicineToEdit != null) {
            etMedicineName.setText(medicineToEdit.getMedicineName());
            etDosage.setText(medicineToEdit.getDosage());
            etQuantity.setText(String.valueOf(medicineToEdit.getTotalQuantity()));
            if (medicineToEdit.getExactTimes() != null) {
                for (String time : medicineToEdit.getExactTimes()) {
                    selectedTimes.add(time);
                    addTimeView(timeSlotsContainer, time, selectedTimes);
                }
            }
        }

        btnAddTime.setOnClickListener(v -> {
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                selectedTimes.add(time);
                addTimeView(timeSlotsContainer, time, selectedTimes);
            }, hour, minute, true); // True for 24 hour time
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();
        });

        builder.setPositiveButton("Save", (dialog, which) -> {
            String medicineName = etMedicineName.getText().toString().trim();
            String dosage = etDosage.getText().toString().trim();
            String quantityStr = etQuantity.getText().toString().trim();

            if (medicineName.isEmpty() || dosage.isEmpty() || quantityStr.isEmpty() || selectedTimes.isEmpty()) {
                Toast.makeText(this, "Please fill all fields and add at least one time", Toast.LENGTH_SHORT).show();
                return;
            }

            int totalQuantity = Integer.parseInt(quantityStr);
            MedicineModel newMedicine = new MedicineModel(medicineName, dosage, selectedTimes, totalQuantity);

            if (medicineToEdit != null) {
                // Remove old, add new (Update)
                db.collection("users").document(residentId)
                        .update("medicines", FieldValue.arrayRemove(medicineToEdit))
                        .addOnSuccessListener(aVoid -> {
                            db.collection("users").document(residentId)
                                    .update("medicines", FieldValue.arrayUnion(newMedicine))
                                    .addOnSuccessListener(aVoid2 -> {
                                        scheduleMedicineNotification(newMedicine);
                                        Toast.makeText(this, "Medicine Updated", Toast.LENGTH_SHORT).show();
                                    });
                        });
            } else {
                db.collection("users").document(residentId)
                        .update("medicines", FieldValue.arrayUnion(newMedicine))
                        .addOnSuccessListener(aVoid -> {
                            scheduleMedicineNotification(newMedicine);
                            Toast.makeText(this, "Medicine Added successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(
                                e -> Toast.makeText(this, "Failed to add medicine", Toast.LENGTH_SHORT).show());
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void addTimeView(LinearLayout container, String time, List<String> selectedTimes) {
        View view = getLayoutInflater().inflate(R.layout.item_medicine_time, container, false);
        TextView tvTime = view.findViewById(R.id.tvTime);
        ImageButton btnRemove = view.findViewById(R.id.btnRemoveTime);

        tvTime.setText(time);
        btnRemove.setOnClickListener(v -> {
            container.removeView(view);
            selectedTimes.remove(time);
        });

        container.addView(view);
    }

    private void scheduleMedicineNotification(MedicineModel medicine) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (medicine.getExactTimes() != null) {
            for (String timeStr : medicine.getExactTimes()) {
                String[] parts = timeStr.split(":");
                if (parts.length == 2) {
                    int hour = Integer.parseInt(parts[0]);
                    int minute = Integer.parseInt(parts[1]);
                    scheduleAlarm(alarmManager, medicine.getMedicineName(), hour, minute);
                }
            }
        }
    }

    private void scheduleAlarm(AlarmManager alarmManager, String medicineName, int hour, int minute) {
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

        int requestCode = (residentId + medicineName + hour + minute).hashCode(); // Unique ID

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
    }
}
