package com.example.sp01.entity;

public class PromotionItem {
    private final String id;
    private final String collectionId;
    private final String imageName;
    private final String title;
    private final String subtitle;

    public PromotionItem(String id, String collectionId, String imageName, String title, String subtitle) {
        this.id = id;
        this.collectionId = collectionId;
        this.imageName = imageName;
        this.title = title;
        this.subtitle = subtitle;
    }

    public String getId() {
        return id;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public String getImageName() {
        return imageName;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }
}
