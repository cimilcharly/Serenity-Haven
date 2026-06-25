package com.example.oldagehome.models;

public class StoreMedicineModel {
    private String name;
    private String category;
    private String description;
    private String price;
    private String platform;
    private String purchaseUrl;
    private String imageResName;
    private String safetyTip;
    private int imageResId;

    public StoreMedicineModel() {
    }

    public StoreMedicineModel(String name, String category, String description, String price, String platform, String purchaseUrl, String imageResName, String safetyTip) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
        this.platform = platform;
        this.purchaseUrl = purchaseUrl;
        this.imageResName = imageResName;
        this.safetyTip = safetyTip;
    }

    public StoreMedicineModel(String name, String description, String price, String platform, String purchaseUrl, int imageResId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.platform = platform;
        this.purchaseUrl = purchaseUrl;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPurchaseUrl() {
        return purchaseUrl;
    }

    public void setPurchaseUrl(String purchaseUrl) {
        this.purchaseUrl = purchaseUrl;
    }

    public String getImageResName() {
        return imageResName;
    }

    public void setImageResName(String imageResName) {
        this.imageResName = imageResName;
    }

    public String getSafetyTip() {
        return safetyTip;
    }

    public void setSafetyTip(String safetyTip) {
        this.safetyTip = safetyTip;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }
}
