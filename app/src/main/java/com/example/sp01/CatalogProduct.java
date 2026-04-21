package com.example.sp01;

public class CatalogProduct {
    private final String title;
    private final String category;
    private final String price;
    private final String description;
    private final String consumption;

    public CatalogProduct(String title, String category, String price, String description, String consumption) {
        this.title = title;
        this.category = category;
        this.price = price;
        this.description = description;
        this.consumption = consumption;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getConsumption() {
        return consumption;
    }
}
