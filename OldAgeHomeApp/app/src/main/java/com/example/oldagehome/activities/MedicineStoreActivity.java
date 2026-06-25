package com.example.oldagehome.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oldagehome.R;
import com.example.oldagehome.adapters.StoreMedicineAdapter;
import com.example.oldagehome.models.StoreMedicineModel;
import com.google.android.material.chip.ChipGroup;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MedicineStoreActivity extends AppCompatActivity {

    private RecyclerView rvMedicines;
    private StoreMedicineAdapter adapter;
    private List<StoreMedicineModel> allMedicines;
    private List<StoreMedicineModel> displayMedicines;
    
    private String currentSearchQuery = "";
    private String currentSelectedCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_store);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        rvMedicines = findViewById(R.id.rvStoreMedicines);
        rvMedicines.setLayoutManager(new LinearLayoutManager(this));

        // Load and setup medicine lists
        allMedicines = loadMedicinesFromJson();
        displayMedicines = new ArrayList<>(allMedicines);

        adapter = new StoreMedicineAdapter(this, displayMedicines);
        rvMedicines.setAdapter(adapter);

        setupSearch();
        setupCategories();
    }

    private List<StoreMedicineModel> loadMedicinesFromJson() {
        List<StoreMedicineModel> list = new ArrayList<>();
        try {
            InputStream is = getAssets().open("medicines.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                StoreMedicineModel med = new StoreMedicineModel(
                    obj.getString("name"),
                    obj.getString("category"),
                    obj.getString("description"),
                    obj.getString("price"),
                    obj.getString("platform"),
                    obj.getString("purchaseUrl"),
                    obj.getString("imageResName"),
                    obj.getString("safetyTip")
                );
                
                // Resolve drawable integer resource ID dynamically by string name
                int resId = getResources().getIdentifier(med.getImageResName(), "drawable", getPackageName());
                med.setImageResId(resId != 0 ? resId : R.drawable.med_tablet_strip);
                
                list.add(med);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private void setupSearch() {
        EditText etSearch = findViewById(R.id.etSearchMedicine);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                filterMedicines();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupCategories() {
        ChipGroup chipGroup = findViewById(R.id.chipGroupCategories);
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                currentSelectedCategory = "All";
            } else if (checkedId == R.id.chipPain) {
                currentSelectedCategory = "Pain Relief";
            } else if (checkedId == R.id.chipDigestion) {
                currentSelectedCategory = "Digestion & Antacids";
            } else if (checkedId == R.id.chipCold) {
                currentSelectedCategory = "Cough & Cold";
            } else if (checkedId == R.id.chipVitamins) {
                currentSelectedCategory = "Vitamins & Supplements";
            } else if (checkedId == R.id.chipFirstAid) {
                currentSelectedCategory = "First Aid & Skin";
            } else if (checkedId == R.id.chipChronic) {
                currentSelectedCategory = "Chronic Care";
            } else {
                currentSelectedCategory = "All"; // Fallback if selection cleared
            }
            filterMedicines();
        });
    }

    private void filterMedicines() {
        displayMedicines.clear();
        String query = currentSearchQuery.toLowerCase().trim();
        
        for (StoreMedicineModel med : allMedicines) {
            boolean matchesCategory = currentSelectedCategory.equals("All") 
                    || med.getCategory().equalsIgnoreCase(currentSelectedCategory);
            
            boolean matchesSearch = query.isEmpty() 
                    || med.getName().toLowerCase().contains(query)
                    || med.getCategory().toLowerCase().contains(query)
                    || med.getDescription().toLowerCase().contains(query);
            
            if (matchesCategory && matchesSearch) {
                displayMedicines.add(med);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
