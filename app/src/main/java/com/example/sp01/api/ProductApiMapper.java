package com.example.sp01.api;

import com.example.sp01.entity.CatalogProduct;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ProductApiMapper {

    private ProductApiMapper() {
    }

    public static List<CatalogProduct> parseProducts(JsonObject response) {
        List<CatalogProduct> result = new ArrayList<>();
        if (response == null || !response.has("items") || !response.get("items").isJsonArray()) {
            return result;
        }

        JsonArray items = response.getAsJsonArray("items");
        for (JsonElement element : items) {
            if (!element.isJsonObject()) {
                continue;
            }
            CatalogProduct product = mapItem(element.getAsJsonObject());
            if (product != null) {
                result.add(product);
            }
        }

        return result;
    }

    private static CatalogProduct mapItem(JsonObject item) {
        String id = firstString(item, "id");
        String title = firstString(item, "title", "name", "product_name");
        String category = firstString(item, "category", "type");
        String typeCloses = firstString(item, "typeCloses", "closes");
        String description = firstString(item, "description", "desc");
        String consumption = firstString(item, "consumption", "material", "weight");
        double priceValue = firstNumber(item, "price", "cost", "amount");

        if (title == null || title.trim().isEmpty()) {
            return null;
        }

        if (id == null || id.trim().isEmpty()) {
            id = title + "_" + category;
        }
        if (category == null || category.trim().isEmpty()) {
            category = "Без категории";
        }
        if (description == null || description.trim().isEmpty()) {
            description = "Описание отсутствует";
        }
        if (consumption == null || consumption.trim().isEmpty()) {
            consumption = "—";
        }

        String priceText = formatPrice(priceValue);
        return new CatalogProduct(id, title, category, typeCloses, priceText, priceValue, description, consumption);
    }

    private static String firstString(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                JsonElement value = obj.get(key);
                if (value.isJsonPrimitive()) {
                    return value.getAsString();
                }
            }
        }
        return null;
    }

    private static double firstNumber(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                JsonElement value = obj.get(key);
                try {
                    if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
                        return value.getAsDouble();
                    }
                    if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                        String normalized = value.getAsString().replace("₽", "").replace(" ", "").replace(",", ".").trim();
                        return Double.parseDouble(normalized);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0d;
    }

    private static String formatPrice(double value) {
        if (value <= 0) {
            return "0 ₽";
        }
        return String.format(Locale.getDefault(), "%.0f ₽", value);
    }
}