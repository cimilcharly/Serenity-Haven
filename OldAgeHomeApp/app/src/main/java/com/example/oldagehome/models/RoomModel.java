package com.example.oldagehome.models;

public class RoomModel {
    private String roomNumber;
    private boolean isOccupied;
    private String residentId; // Null if empty

    public RoomModel() {
    }

    public RoomModel(String roomNumber, boolean isOccupied, String residentId) {
        this.roomNumber = roomNumber;
        this.isOccupied = isOccupied;
        this.residentId = residentId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }

    public String getResidentId() {
        return residentId;
    }

    public void setResidentId(String residentId) {
        this.residentId = residentId;
    }
}
