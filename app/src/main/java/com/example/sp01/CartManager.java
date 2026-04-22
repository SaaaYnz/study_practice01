package com.example.sp01;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.sp01.entity.CatalogProduct;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CartManager {

    private static final String PREF_NAME = "cart_state";
    private static final String KEY_ITEMS = "items_json";

    private static final Map<String, CartEntry> ITEMS = new LinkedHashMap<>();
    private static boolean loaded;

    private CartManager() {
    }

    public static synchronized void toggle(Context context, CatalogProduct product) {
        ensureLoaded(context);
        if (ITEMS.containsKey(product.getId())) {
            ITEMS.remove(product.getId());
        } else {
            ITEMS.put(product.getId(), new CartEntry(product, 1));
        }
        save(context);
    }

    public static synchronized void add(Context context, CatalogProduct product) {
        ensureLoaded(context);
        CartEntry entry = ITEMS.get(product.getId());
        if (entry == null) {
            ITEMS.put(product.getId(), new CartEntry(product, 1));
        } else {
            entry.quantity += 1;
        }
        save(context);
    }

    public static synchronized void increment(Context context, String productId) {
        ensureLoaded(context);
        CartEntry entry = ITEMS.get(productId);
        if (entry == null) {
            return;
        }
        entry.quantity += 1;
        save(context);
    }

    public static synchronized void decrement(Context context, String productId) {
        ensureLoaded(context);
        CartEntry entry = ITEMS.get(productId);
        if (entry == null) {
            return;
        }
        entry.quantity -= 1;
        if (entry.quantity <= 0) {
            ITEMS.remove(productId);
        }
        save(context);
    }

    public static synchronized void remove(Context context, String productId) {
        ensureLoaded(context);
        ITEMS.remove(productId);
        save(context);
    }

    public static synchronized void clear(Context context) {
        ensureLoaded(context);
        ITEMS.clear();
        save(context);
    }

    public static synchronized boolean isInCart(Context context, String productId) {
        ensureLoaded(context);
        return ITEMS.containsKey(productId);
    }

    public static synchronized int getTotalQuantity(Context context) {
        ensureLoaded(context);
        int total = 0;
        for (CartEntry entry : ITEMS.values()) {
            total += entry.quantity;
        }
        return total;
    }

    public static synchronized double getTotalPrice(Context context) {
        ensureLoaded(context);
        double total = 0d;
        for (CartEntry entry : ITEMS.values()) {
            total += entry.product.getPriceValue() * entry.quantity;
        }
        return total;
    }

    public static synchronized String getTotalPriceText(Context context) {
        return String.format(Locale.getDefault(), "%.0f ₽", getTotalPrice(context));
    }

    public static synchronized List<CartEntry> getEntries(Context context) {
        ensureLoaded(context);
        return new ArrayList<>(ITEMS.values());
    }

    private static void ensureLoaded(Context context) {
        if (loaded) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY_ITEMS, "[]");
        ITEMS.clear();

        try {
            JsonElement root = JsonParser.parseString(raw);
            if (root.isJsonArray()) {
                JsonArray array = root.getAsJsonArray();
                for (JsonElement element : array) {
                    if (!element.isJsonObject()) {
                        continue;
                    }
                    JsonObject obj = element.getAsJsonObject();
                    CatalogProduct product = fromJson(obj);
                    if (product == null) {
                        continue;
                    }
                    int qty = obj.has("quantity") ? obj.get("quantity").getAsInt() : 1;
                    if (qty <= 0) {
                        continue;
                    }
                    ITEMS.put(product.getId(), new CartEntry(product, qty));
                }
            }
        } catch (Exception ignored) {
        }

        loaded = true;
    }

    private static void save(Context context) {
        JsonArray array = new JsonArray();
        for (CartEntry entry : ITEMS.values()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", entry.product.getId());
            obj.addProperty("title", entry.product.getTitle());
            obj.addProperty("category", entry.product.getCategory());
            obj.addProperty("price", entry.product.getPrice());
            obj.addProperty("priceValue", entry.product.getPriceValue());
            obj.addProperty("description", entry.product.getDescription());
            obj.addProperty("consumption", entry.product.getConsumption());
            obj.addProperty("quantity", entry.quantity);
            array.add(obj);
        }

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ITEMS, array.toString())
                .apply();
    }

    private static CatalogProduct fromJson(JsonObject obj) {
        if (!obj.has("id") || !obj.has("title")) {
            return null;
        }
        String id = obj.get("id").getAsString();
        String title = getString(obj, "title");
        String category = getString(obj, "category");
        String typeCloses = getString(obj, "typeCloses");
        String price = getString(obj, "price");
        double priceValue = obj.has("priceValue") ? obj.get("priceValue").getAsDouble() : 0d;
        String description = getString(obj, "description");
        String consumption = getString(obj, "consumption");
        return new CatalogProduct(id, title, category, typeCloses, price, priceValue, description, consumption);
    }

    private static String getString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    public static final class CartEntry {
        public final CatalogProduct product;
        public int quantity;

        public CartEntry(CatalogProduct product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }
}
