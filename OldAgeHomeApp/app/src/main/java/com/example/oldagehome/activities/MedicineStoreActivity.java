package com.example.oldagehome.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.oldagehome.R;
import com.example.oldagehome.adapters.StoreMedicineAdapter;
import com.example.oldagehome.models.StoreMedicineModel;
import java.util.ArrayList;
import java.util.List;

public class MedicineStoreActivity extends AppCompatActivity {

    private RecyclerView rvMedicines;
    private StoreMedicineAdapter adapter;
    private List<StoreMedicineModel> medicineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_store);

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());

        rvMedicines = findViewById(R.id.rvStoreMedicines);
        rvMedicines.setLayoutManager(new LinearLayoutManager(this));

        medicineList = new ArrayList<>();
        populateStoreItems();

        adapter = new StoreMedicineAdapter(this, medicineList);
        rvMedicines.setAdapter(adapter);
    }

    private void populateStoreItems() {
        medicineList.add(new StoreMedicineModel(
                "Crocin Pain Relief",
                "Contains Paracetamol & Caffeine for fast relief from headache and body pain.",
                "₹38",
                "Tata 1mg",
                "https://www.1mg.com/otc/crocin-pain-relief-tablet-otc330089",
                R.drawable.med_tablet_strip
        ));

        medicineList.add(new StoreMedicineModel(
                "Combiflam Tablets",
                "Effective relief from muscular pain, joint pain, dental pain, and fever.",
                "₹45",
                "Tata 1mg",
                "https://www.1mg.com/drugs/combiflam-tablet-63462",
                R.drawable.med_tablet_strip
        ));

        medicineList.add(new StoreMedicineModel(
                "Supradyn Daily Multivitamin",
                "Essential multivitamins and minerals to boost daily energy and immune system.",
                "₹110",
                "Amazon India",
                "https://www.amazon.in/s?k=Supradyn+Daily+Multivitamin",
                R.drawable.med_capsule_bottle
        ));

        medicineList.add(new StoreMedicineModel(
                "Digene Antacid Tablets (Mint)",
                "Provides quick relief from acidity, heartburn, gas, and stomach discomfort.",
                "₹24",
                "Amazon India",
                "https://www.amazon.in/s?k=Digene+Antacid+Tablets",
                R.drawable.med_antacid_box
        ));

        medicineList.add(new StoreMedicineModel(
                "Volini Pain Relief Gel",
                "Quick action gel for back pain, joint pain, neck pain, and sprains.",
                "₹145",
                "Flipkart",
                "https://www.flipkart.com/search?q=Volini+Pain+Relief+Gel",
                R.drawable.med_ointment_tube
        ));

        medicineList.add(new StoreMedicineModel(
                "Benadryl DR Cough Syrup",
                "Fast-acting relief from dry cough, throat irritation, and sneezing.",
                "₹125",
                "Flipkart",
                "https://www.flipkart.com/search?q=Benadryl+Cough+Syrup",
                R.drawable.med_syrup_bottle
        ));

        medicineList.add(new StoreMedicineModel(
                "Dettol Antiseptic Liquid",
                "First aid antiseptic for cuts, bites, grazes, and personal hygiene use.",
                "₹85",
                "Amazon India",
                "https://www.amazon.in/s?k=Dettol+Antiseptic+Liquid",
                R.drawable.med_syrup_bottle
        ));

        medicineList.add(new StoreMedicineModel(
                "Moov Pain Relief Spray",
                "100% Ayurvedic preparation for fast relief from muscle pain and backache.",
                "₹180",
                "Amazon India",
                "https://www.amazon.in/s?k=Moov+Pain+Relief+Spray",
                R.drawable.med_ointment_tube
        ));

        medicineList.add(new StoreMedicineModel(
                "Vicks Vaporub",
                "Ayurvedic ointment providing relief from cold symptoms, cough, and body ache.",
                "₹95",
                "Amazon India",
                "https://www.amazon.in/s?k=Vicks+Vaporub",
                R.drawable.med_vaporub_jar
        ));

        medicineList.add(new StoreMedicineModel(
                "Eno Fruit Salt (Lemon)",
                "Quick-acting antacid powder that relieves acidity and stomach gas in 6 seconds.",
                "₹60",
                "Tata 1mg",
                "https://www.1mg.com/otc/eno-lemon-fruit-salt-powder-otc118671",
                R.drawable.med_powder_sachet
        ));

        medicineList.add(new StoreMedicineModel(
                "Zandu Balm",
                "India's No. 1 pain relief balm for headache, body ache, and cold symptoms.",
                "₹50",
                "Flipkart",
                "https://www.flipkart.com/search?q=Zandu+Balm",
                R.drawable.med_vaporub_jar
        ));

        medicineList.add(new StoreMedicineModel(
                "Strepsils Ginger Lozenges",
                "Sore throat lozenges that provide quick relief from throat irritation and cough.",
                "₹35",
                "Tata 1mg",
                "https://www.1mg.com/otc/strepsils-ginger-lemon-lozenges-otc327299",
                R.drawable.med_lozenges_pack
        ));

        medicineList.add(new StoreMedicineModel(
                "Saridon Headache Relief",
                "Fast-acting headache relief tablet with a unique triple action formula.",
                "₹42",
                "Tata 1mg",
                "https://www.1mg.com/drugs/saridon-tablet-134375",
                R.drawable.med_tablet_strip
        ));

        medicineList.add(new StoreMedicineModel(
                "Eno Powder Sachets (Regular)",
                "Effervescent antacid powder for fast relief from bloating, gas, and acidity.",
                "₹8",
                "Amazon India",
                "https://www.amazon.in/s?k=Eno+Regular+Sachets",
                R.drawable.med_powder_sachet
        ));

        medicineList.add(new StoreMedicineModel(
                "Revital H Capsules",
                "Daily health supplement with Ginseng, multivitamins, and minerals to stay active.",
                "₹310",
                "Amazon India",
                "https://www.amazon.in/s?k=Revital+H+Capsules",
                R.drawable.med_capsule_bottle
        ));

        medicineList.add(new StoreMedicineModel(
                "Pudin Hara Active",
                "Quick Ayurvedic relief from stomach ache, gas, and indigestion.",
                "₹25",
                "Tata 1mg",
                "https://www.1mg.com/otc/dabur-pudin-hara-active-liquid-otc120894",
                R.drawable.med_syrup_bottle
        ));

        medicineList.add(new StoreMedicineModel(
                "Alerid Cetirizine Tablets",
                "Anti-allergic medicine for relief from runny nose, sneezing, and watery eyes.",
                "₹18",
                "Tata 1mg",
                "https://www.1mg.com/drugs/alerid-10mg-tablet-69335",
                R.drawable.med_tablet_strip
        ));

        medicineList.add(new StoreMedicineModel(
                "ORS Lytol Powder",
                "WHO formula Oral Rehydration Salts to restore body fluids and electrolytes.",
                "₹22",
                "Amazon India",
                "https://www.amazon.in/s?k=ORS+Powder",
                R.drawable.med_powder_sachet
        ));
    }
}
