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

        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        android.widget.AutoCompleteTextView etMedicineName = dialogView.findViewById(R.id.etMedicineName);
        com.google.android.material.textfield.TextInputEditText etDosageValue = dialogView.findViewById(R.id.etDosageValue);
        com.google.android.material.chip.ChipGroup chipGroupUnit = dialogView.findViewById(R.id.chipGroupUnit);
        com.google.android.material.textfield.TextInputEditText etQuantity = dialogView.findViewById(R.id.etQuantity);
        LinearLayout timeSlotsContainer = dialogView.findViewById(R.id.timeSlotsContainer);
        Button btnAddTime = dialogView.findViewById(R.id.btnAddTime);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnSave = dialogView.findViewById(R.id.btnSave);

        android.widget.ImageButton btnDosageUp = dialogView.findViewById(R.id.btnDosageUp);
        android.widget.ImageButton btnDosageDown = dialogView.findViewById(R.id.btnDosageDown);
        android.widget.ImageButton btnQtyUp = dialogView.findViewById(R.id.btnQtyUp);
        android.widget.ImageButton btnQtyDown = dialogView.findViewById(R.id.btnQtyDown);

        btnDosageUp.setOnClickListener(v -> {
            String valStr = etDosageValue.getText().toString().trim();
            double val = 0.0;
            if (!valStr.isEmpty()) {
                try {
                    val = Double.parseDouble(valStr);
                } catch (NumberFormatException ignored) {}
            }
            int checkedChipId = chipGroupUnit.getCheckedChipId();
            double increment = 1.0;
            if (checkedChipId == R.id.chipMg) {
                increment = 50.0;
            } else if (checkedChipId == R.id.chipMl) {
                increment = 5.0;
            }
            val += increment;
            if (val == (long) val) {
                etDosageValue.setText(String.valueOf((long) val));
            } else {
                etDosageValue.setText(String.valueOf(val));
            }
        });

        btnDosageDown.setOnClickListener(v -> {
            String valStr = etDosageValue.getText().toString().trim();
            double val = 0.0;
            if (!valStr.isEmpty()) {
                try {
                    val = Double.parseDouble(valStr);
                } catch (NumberFormatException ignored) {}
            }
            int checkedChipId = chipGroupUnit.getCheckedChipId();
            double decrement = 1.0;
            if (checkedChipId == R.id.chipMg) {
                decrement = 50.0;
            } else if (checkedChipId == R.id.chipMl) {
                decrement = 5.0;
            }
            val -= decrement;
            if (val < 0) val = 0.0;
            if (val == (long) val) {
                etDosageValue.setText(String.valueOf((long) val));
            } else {
                etDosageValue.setText(String.valueOf(val));
            }
        });

        btnQtyUp.setOnClickListener(v -> {
            String qtyStr = etQuantity.getText().toString().trim();
            int qty = 0;
            if (!qtyStr.isEmpty()) {
                try {
                    qty = Integer.parseInt(qtyStr);
                } catch (NumberFormatException ignored) {}
            }
            qty += 10;
            etQuantity.setText(String.valueOf(qty));
        });

        btnQtyDown.setOnClickListener(v -> {
            String qtyStr = etQuantity.getText().toString().trim();
            int qty = 0;
            if (!qtyStr.isEmpty()) {
                try {
                    qty = Integer.parseInt(qtyStr);
                } catch (NumberFormatException ignored) {}
            }
            qty -= 10;
            if (qty < 0) qty = 0;
            etQuantity.setText(String.valueOf(qty));
        });

        List<String> medicineSuggestions = getMedicineSuggestions();
        android.widget.ArrayAdapter<String> suggestionAdapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, medicineSuggestions);
        etMedicineName.setAdapter(suggestionAdapter);
        etMedicineName.setThreshold(1);

        List<String> selectedTimes = new ArrayList<>();

        if (medicineToEdit != null) {
            if (tvDialogTitle != null) {
                tvDialogTitle.setText("Edit Medicine");
            }
            etMedicineName.setText(medicineToEdit.getMedicineName());

            // Parse dosage value and unit
            String dosageStr = medicineToEdit.getDosage() != null ? medicineToEdit.getDosage().trim() : "";
            String dosageValue = "";
            String dosageUnit = "";
            int lastSpaceIdx = dosageStr.lastIndexOf(' ');
            if (lastSpaceIdx != -1) {
                dosageValue = dosageStr.substring(0, lastSpaceIdx).trim();
                dosageUnit = dosageStr.substring(lastSpaceIdx + 1).trim();
            } else {
                int i = 0;
                while (i < dosageStr.length() && (Character.isDigit(dosageStr.charAt(i)) || dosageStr.charAt(i) == '.')) {
                    i++;
                }
                dosageValue = dosageStr.substring(0, i).trim();
                dosageUnit = dosageStr.substring(i).trim();
            }

            etDosageValue.setText(dosageValue);

            if (dosageUnit.equalsIgnoreCase("ml")) {
                chipGroupUnit.check(R.id.chipMl);
            } else if (dosageUnit.equalsIgnoreCase("mg")) {
                chipGroupUnit.check(R.id.chipMg);
            } else if (dosageUnit.toLowerCase().contains("capsule")) {
                chipGroupUnit.check(R.id.chipCapsule);
            } else if (dosageUnit.toLowerCase().contains("tablet")) {
                chipGroupUnit.check(R.id.chipTablet);
            } else {
                chipGroupUnit.check(R.id.chipTablet);
            }

            etQuantity.setText(String.valueOf(medicineToEdit.getTotalQuantity()));
            if (medicineToEdit.getExactTimes() != null) {
                for (String time : medicineToEdit.getExactTimes()) {
                    selectedTimes.add(time);
                    addTimeView(timeSlotsContainer, time, selectedTimes);
                }
            }
        } else {
            chipGroupUnit.check(R.id.chipTablet);
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

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String medicineName = etMedicineName.getText().toString().trim();
            String val = etDosageValue.getText().toString().trim();
            String quantityStr = etQuantity.getText().toString().trim();

            int checkedChipId = chipGroupUnit.getCheckedChipId();
            if (medicineName.isEmpty() || val.isEmpty() || quantityStr.isEmpty() || selectedTimes.isEmpty() || checkedChipId == View.NO_ID) {
                Toast.makeText(this, "Please fill all fields, select a unit, and add at least one time", Toast.LENGTH_SHORT).show();
                return;
            }

            // Determine unit and plurals
            String unit = "";
            if (checkedChipId == R.id.chipMl) {
                unit = "ml";
            } else if (checkedChipId == R.id.chipMg) {
                unit = "mg";
            } else if (checkedChipId == R.id.chipCapsule) {
                try {
                    double parsedVal = Double.parseDouble(val);
                    if (parsedVal == 1.0) {
                        unit = "Capsule";
                    } else {
                        unit = "Capsules";
                    }
                } catch (NumberFormatException e) {
                    unit = "Capsules";
                }
            } else if (checkedChipId == R.id.chipTablet) {
                try {
                    double parsedVal = Double.parseDouble(val);
                    if (parsedVal == 1.0) {
                        unit = "Tablet";
                    } else {
                        unit = "Tablets";
                    }
                } catch (NumberFormatException e) {
                    unit = "Tablets";
                }
            }

            String dosage = val + " " + unit;
            int totalQuantity;
            try {
                totalQuantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
                return;
            }

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
                                        dialog.dismiss();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to update medicine details", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to remove old medicine details", Toast.LENGTH_SHORT).show();
                        });
            } else {
                db.collection("users").document(residentId)
                        .update("medicines", FieldValue.arrayUnion(newMedicine))
                        .addOnSuccessListener(aVoid -> {
                            scheduleMedicineNotification(newMedicine);
                            Toast.makeText(this, "Medicine Added successfully", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(
                                e -> Toast.makeText(this, "Failed to add medicine", Toast.LENGTH_SHORT).show());
            }
        });

        dialog.show();
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

    private List<String> getMedicineSuggestions() {
        List<String> suggestions = new ArrayList<>();
        try {
            java.io.InputStream is = getAssets().open("medicines.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, java.nio.charset.StandardCharsets.UTF_8);
            org.json.JSONArray jsonArray = new org.json.JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                org.json.JSONObject obj = jsonArray.getJSONObject(i);
                String name = obj.optString("name");
                if (name != null && !name.isEmpty() && !suggestions.contains(name)) {
                    suggestions.add(name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Add a few fallback defaults if loading fails
        if (suggestions.isEmpty()) {
            suggestions.add("Paracetamol");
            suggestions.add("Ibuprofen");
            suggestions.add("Aspirin");
            suggestions.add("Supradyn");
            suggestions.add("Metformin");
            suggestions.add("Atorvastatin");
            suggestions.add("Pantocid");
            suggestions.add("Volini");
            suggestions.add("Benadryl");
            suggestions.add("Limcee");
        }
        return suggestions;
    }
}
