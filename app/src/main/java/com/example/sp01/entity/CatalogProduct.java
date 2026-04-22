package com.example.sp01.entity;

public class CatalogProduct {
    private final String id;
    private final String title;
    private final String category;
    private final String typeCloses;
    private final String price;
    private final double priceValue;
    private final String description;
    private final String consumption;

    public CatalogProduct(String id,
                          String title,
                          String category,
                          String typeCloses,
                          String price,
                          double priceValue,
                          String description,
                          String consumption) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.typeCloses = typeCloses;
        this.price = price;
        this.priceValue = priceValue;
        this.description = description;
        this.consumption = consumption;
    }

    public CatalogProduct(String title,
                          String category,
                          String typeCloses,
                          String price,
                          String description,
                          String consumption) {
        this(
                title + "_" + category,
                title,
                category,
                typeCloses,
                price,
                parsePriceValue(price),
                description,
                consumption
        );
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getTypeCloses() {
        return typeCloses;
    }

    public String getPrice() {
        return price;
    }

    public double getPriceValue() {
        return priceValue;
    }

    public String getDescription() {
        return description;
    }

    public String getConsumption() {
        return consumption;
    }

    private static double parsePriceValue(String price) {
        if (price == null) {
            return 0d;
        }

        String normalized = price
                .replace("₽", "")
                .replace(" ", "")
                .replace(",", ".")
                .trim();

        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return 0d;
        }
    }
}