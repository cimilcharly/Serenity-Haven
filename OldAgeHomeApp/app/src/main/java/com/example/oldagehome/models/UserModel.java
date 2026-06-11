package com.example.oldagehome.models;

import java.util.List;

public class UserModel {
    private String uid;
    private String name;
    private String email;
    private String role; // "mainstaff" or "resident"
    private String status; // "pending" or "approved"
    private String communityName;
    private String communityId;

    // Resident-specific fields
    private int age;
    private String gender;
    private String roomNumber;
    private String medicalHistory;
    private String emergencyContact;
    private long admissionDate;
    private String doctorVisitTime;
    private String doctorVisitDate;
    private String profileImageUrl;
    private List<MedicineModel> medicines;
    private String createdBy; // "Main Staff" or "Resident"

    public UserModel() {
        // Default constructor required for calls to
        // DataSnapshot.getValue(UserModel.class)
    }

    public UserModel(String uid, String email, String role, String name) {
        this.uid = uid;
        this.email = email;
        this.role = role;
        this.name = name;
    }

    // Getters and Setters

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getDoctorVisitTime() {
        return doctorVisitTime;
    }

    public void setDoctorVisitTime(String doctorVisitTime) {
        this.doctorVisitTime = doctorVisitTime;
    }

    public String getDoctorVisitDate() {
        return doctorVisitDate;
    }

    public void setDoctorVisitDate(String doctorVisitDate) {
        this.doctorVisitDate = doctorVisitDate;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    private String doctorName;

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public List<MedicineModel> getMedicines() {
        return medicines;
    }

    public void setMedicines(List<MedicineModel> medicines) {
        this.medicines = medicines;
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
