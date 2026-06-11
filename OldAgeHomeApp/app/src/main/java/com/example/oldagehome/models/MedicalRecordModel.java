package com.example.oldagehome.models;

import java.util.List;

public class MedicalRecordModel {
    private String id;
    private String residentId;
    private String doctorId; // Who added it
    private long timestamp;
    private String notes;
    private List<String> prescriptions;

    public MedicalRecordModel() {
    }

    public MedicalRecordModel(String id, String residentId, String doctorId, long timestamp, String notes,
            List<String> prescriptions) {
        this.id = id;
        this.residentId = residentId;
        this.doctorId = doctorId;
        this.timestamp = timestamp;
        this.notes = notes;
        this.prescriptions = prescriptions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResidentId() {
        return residentId;
    }

    public void setResidentId(String residentId) {
        this.residentId = residentId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<String> getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(List<String> prescriptions) {
        this.prescriptions = prescriptions;
    }
}
