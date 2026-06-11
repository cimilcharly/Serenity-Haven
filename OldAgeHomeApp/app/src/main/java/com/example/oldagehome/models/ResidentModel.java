package com.example.oldagehome.models;

import java.util.List;

public class ResidentModel {
    private String id;
    private String name;
    private int age;
    private String gender;
    private String roomNumber;
    private String medicalHistory;
    private String emergencyContact;
    private long admissionDate;
    private String doctorId; // Assigned doctor
    private String role; // "staff" or "resident"
    private String status; // "pending" or "approved"
    private String password;
    private String profileImageUrl;
    private List<MedicineModel> medicines;
    private String createdBy;
    private String communityName;
    private String communityId;

    public ResidentModel() {
    }

    public ResidentModel(String id, String name, int age, String gender, String roomNumber, String medicalHistory,
            String emergencyContact, long admissionDate, String doctorId, String role, String status, String password,
            List<MedicineModel> medicines) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.roomNumber = roomNumber;
        this.medicalHistory = medicalHistory;
        this.emergencyContact = emergencyContact;
        this.admissionDate = admissionDate;
        this.doctorId = doctorId;
        this.role = role;
        this.status = status;
        this.password = password;
        this.medicines = medicines;
    }

    // Getters and Setters

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public long getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(long admissionDate) {
        this.admissionDate = admissionDate;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String doctorVisitDate;
    private String doctorVisitTime;

    public String getDoctorVisitDate() {
        return doctorVisitDate;
    }

    public void setDoctorVisitDate(String doctorVisitDate) {
        this.doctorVisitDate = doctorVisitDate;
    }

    public String getDoctorVisitTime() {
        return doctorVisitTime;
    }

    public void setDoctorVisitTime(String doctorVisitTime) {
        this.doctorVisitTime = doctorVisitTime;
    }

    public List<MedicineModel> getMedicines() {
        return medicines;
    }

    public void setMedicines(List<MedicineModel> medicines) {
        this.medicines = medicines;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCommunityName() {
        return communityName;
    }

    public void setCommunityName(String communityName) {
        this.communityName = communityName;
    }

    public String getCommunityId() {
        return communityId;
    }

    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }
}
