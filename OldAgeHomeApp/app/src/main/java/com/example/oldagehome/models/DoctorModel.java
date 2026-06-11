package com.example.oldagehome.models;

public class DoctorModel {
    private String id;
    private String name;
    private String specialization;
    private String contact;
    private String availableDays;
    private String availableTime;

    public DoctorModel() {
    }

    public DoctorModel(String id, String name, String specialization, String contact, String availableDays,
            String availableTime) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.contact = contact;
        this.availableDays = availableDays;
        this.availableTime = availableTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(String availableDays) {
        this.availableDays = availableDays;
    }

    public String getAvailableTime() {
        return availableTime;
    }

    public void setAvailableTime(String availableTime) {
        this.availableTime = availableTime;
    }
}
