package com.example.sp01;

import android.content.Context;
import android.text.TextUtils;

import com.example.sp01.api.ApiClient;
import com.example.sp01.entity.CatalogProduct;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class OrderManager {

    private OrderManager() {
    }

    public interface OrdersCallback {
        void onComplete(List<OrderEntry> orders, String errorMessage);
    }

    public interface ActionCallback {
        void onComplete(boolean success, String errorMessage);
    }

    public static void getOrders(Context context, OrdersCallback callback) {
        String authorization = AuthSession.getAuthorization(context);
        if (TextUtils.isEmpty(authorization)) {
            callback.onComplete(new ArrayList<>(), "Нет авторизации");
            return;
        }

        ApiClient.getApiService().getOrders(authorization, 1, 30).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onComplete(parseOrders(response.body()), null);
                } else {
                    callback.onComplete(new ArrayList<>(), "Не удалось загрузить заказы");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onComplete(new ArrayList<>(), t.getMessage());
            }
        });
    }

    public static void checkoutFromCart(Context context, ActionCallback callback) {
        String authorization = AuthSession.getAuthorization(context);
        String userId = AuthSession.getUserId(context);
        List<CartManager.CartEntry> entries = CartManager.getEntries(context);

        if (TextUtils.isEmpty(authorization) || TextUtils.isEmpty(userId)) {
            callback.onComplete(false, "Нет данных пользователя");
            return;
        }
        if (entries.isEmpty()) {
            callback.onComplete(false, "Корзина пуста");
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("user_id", userId);
        JsonArray items = new JsonArray();
        double total = 0d;

        for (CartManager.CartEntry entry : entries) {
            JsonObject item = new JsonObject();
            item.addProperty("product_id", entry.product.getId());
            item.addProperty("title", entry.product.getTitle());
            item.addProperty("price", entry.product.getPriceValue());
            item.addProperty("quantity", entry.quantity);
            items.add(item);
            total += entry.product.getPriceValue() * entry.quantity;
        }

        body.add("items", items);
        body.addProperty("total", total);

        ApiClient.getApiService().createOrder(authorization, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!response.isSuccessful()) {
                    callback.onComplete(false, "Не удалось создать заказ");
                    return;
                }

                CartManager.clear(context, (success, errorMessage) ->
                        callback.onComplete(success, success ? null : errorMessage));
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onComplete(false, t.getMessage());
            }
        });
    }

    private static List<OrderEntry> parseOrders(JsonObject response) {
        List<OrderEntry> result = new ArrayList<>();
        if (!response.has("items") || !response.get("items").isJsonArray()) {
            return result;
        }

        JsonArray orders = response.getAsJsonArray("items");
        for (JsonElement orderElement : orders) {
            if (!orderElement.isJsonObject()) {
                continue;
            }

            JsonObject orderObject = orderElement.getAsJsonObject();
            List<OrderLine> lines = new ArrayList<>();
            if (orderObject.has("items") && orderObject.get("items").isJsonArray()) {
                JsonArray items = orderObject.getAsJsonArray("items");
                for (JsonElement itemElement : items) {
                    if (!itemElement.isJsonObject()) {
                        continue;
                    }
                    JsonObject itemObject = itemElement.getAsJsonObject();
                    double price = itemObject.has("price") ? itemObject.get("price").getAsDouble() : 0d;
                    CatalogProduct product = new CatalogProduct(
                            getString(itemObject, "product_id"),
                            getString(itemObject, "title"),
                            "",
                            "",
                            String.format(Locale.getDefault(), "%.0f ₽", price),
                            price,
                            "",
                            ""
                    );
                    lines.add(new OrderLine(product, itemObject.has("quantity") ? itemObject.get("quantity").getAsInt() : 0));
                }
            }

            result.add(new OrderEntry(
                    getString(orderObject, "id"),
                    getString(orderObject, "created"),
                    orderObject.has("total") ? orderObject.get("total").getAsDouble() : 0d,
                    lines
            ));
        }
        return result;
    }

    private static String getString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }

    public static final class OrderEntry {
        public final String id;
        public final String createdAt;
        public final double total;
        public final List<OrderLine> items;

        public OrderEntry(String id, String createdAt, double total, List<OrderLine> items) {
            this.id = id;
            this.createdAt = createdAt;
            this.total = total;
            this.items = items;
        }

        public int getTotalQuantity() {
            int totalQuantity = 0;
            for (OrderLine item : items) {
                totalQuantity += item.quantity;
            }
            return totalQuantity;
        }

        public String getTotalPriceText() {
            return String.format(Locale.getDefault(), "%.0f ₽", total);
        }
    }

    public static final class OrderLine {
        public final CatalogProduct product;
        public final int quantity;

        public OrderLine(CatalogProduct product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }
}
