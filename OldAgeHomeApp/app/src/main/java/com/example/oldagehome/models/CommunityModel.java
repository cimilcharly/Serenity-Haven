package com.example.oldagehome.models;

public class CommunityModel {
    private String id;
    private String name;
    private String creatorUid;
    private String joinCode;
    private long createdAt;

    public CommunityModel() {
        // Default constructor required for Firebase serialization
    }

    public CommunityModel(String id, String name, String creatorUid, String joinCode, long createdAt) {
        this.id = id;
        this.name = name;
        this.creatorUid = creatorUid;
        this.joinCode = joinCode;
        this.createdAt = createdAt;
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

    public String getCreatorUid() {
        return creatorUid;
    }

    public void setCreatorUid(String creatorUid) {
        this.creatorUid = creatorUid;
    }

    public String getJoinCode() {
        return joinCode;
    }

    public void setJoinCode(String joinCode) {
        this.joinCode = joinCode;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
