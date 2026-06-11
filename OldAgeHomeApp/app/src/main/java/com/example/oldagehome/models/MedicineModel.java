package com.example.oldagehome.models;

import java.util.List;

public class MedicineModel {
    private String medicineName;
    private String dosage;
    private int totalQuantity;
    private List<String> exactTimes; // Format "HH:mm"

    public MedicineModel() {
    }

    public MedicineModel(String medicineName, String dosage, List<String> exactTimes, int totalQuantity) {
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.exactTimes = exactTimes;
        this.totalQuantity = totalQuantity;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public List<String> getExactTimes() {
        return exactTimes;
    }

    public void setExactTimes(List<String> exactTimes) {
        this.exactTimes = exactTimes;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}
