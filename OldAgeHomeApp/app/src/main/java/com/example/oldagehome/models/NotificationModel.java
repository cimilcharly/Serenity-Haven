package com.example.oldagehome.models;

public class NotificationModel {
    private String id;
    private String recipientId;
    private String message;
    private String type;
    private String relatedId;
    private boolean isRead;
    private long timestamp;

    public NotificationModel() {
    }

    public NotificationModel(String id, String recipientId, String message, String type, String relatedId,
            boolean isRead, long timestamp) {
        this.id = id;
        this.recipientId = recipientId;
        this.message = message;
        this.type = type;
        this.relatedId = relatedId;
        this.isRead = isRead;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(String relatedId) {
        this.relatedId = relatedId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
