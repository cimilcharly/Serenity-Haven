package com.example.oldagehome.models;

public class AppointmentModel {
    private String id;
    private String residentId;
    private String doctorId;
    private long timestamp;
    private String notes;
    private String status; // Pending, Confirmed, Completed, Cancelled

    public AppointmentModel() {
    }

    public AppointmentModel(String id, String residentId, String doctorId, long timestamp, String notes,
            String status) {
        this.id = id;
        this.residentId = residentId;
        this.doctorId = doctorId;
        this.timestamp = timestamp;
        this.notes = notes;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
