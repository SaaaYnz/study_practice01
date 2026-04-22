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

public final class CartManager {

    private static final List<CartEntry> ITEMS = new ArrayList<>();
    private static String basketId;
    private static boolean loaded;

    private CartManager() {
    }

    public interface ResultCallback {
        void onComplete(boolean success, String errorMessage);
    }

    public static void load(Context context, ResultCallback callback) {
        String authorization = AuthSession.getAuthorization(context);
        if (TextUtils.isEmpty(authorization)) {
            synchronized (CartManager.class) {
                clearCache();
                loaded = true;
            }
            callback.onComplete(false, "Нет авторизации");
            return;
        }

        ApiClient.getApiService().getBasket(authorization).enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                synchronized (CartManager.class) {
                    if (response.isSuccessful() && response.body() != null) {
                        updateCacheFromResponse(response.body());
                        loaded = true;
                        callback.onComplete(true, null);
                    } else {
                        clearCache();
                        loaded = true;
                        callback.onComplete(false, "Не удалось загрузить корзину");
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                synchronized (CartManager.class) {
                    clearCache();
                    loaded = true;
                }
                callback.onComplete(false, t.getMessage());
            }
        });
    }

    public static void toggle(Context context, CatalogProduct product) {
        toggle(context, product, (success, errorMessage) -> {
        });
    }

    public static void toggle(Context context, CatalogProduct product, ResultCallback callback) {
        withLoaded(context, (success, errorMessage) -> {
            if (!success) {
                callback.onComplete(false, errorMessage);
                return;
            }

            List<CartEntry> next = copyItems();
            int index = findIndex(product.getId(), next);
            if (index >= 0) {
                next.remove(index);
            } else {
                next.add(new CartEntry(product, 1));
            }
            persist(context, next, callback);
        });
    }

    public static void increment(Context context, String productId) {
        increment(context, productId, (success, errorMessage) -> {
        });
    }

    public static void increment(Context context, String productId, ResultCallback callback) {
        withLoaded(context, (success, errorMessage) -> {
            if (!success) {
                callback.onComplete(false, errorMessage);
                return;
            }

            List<CartEntry> next = copyItems();
            int index = findIndex(productId, next);
            if (index >= 0) {
                next.get(index).quantity += 1;
            }
            persist(context, next, callback);
        });
    }

    public static void decrement(Context context, String productId) {
        decrement(context, productId, (success, errorMessage) -> {
        });
    }

    public static void decrement(Context context, String productId, ResultCallback callback) {
        withLoaded(context, (success, errorMessage) -> {
            if (!success) {
                callback.onComplete(false, errorMessage);
                return;
            }

            List<CartEntry> next = copyItems();
            int index = findIndex(productId, next);
            if (index >= 0) {
                next.get(index).quantity -= 1;
                if (next.get(index).quantity <= 0) {
                    next.remove(index);
                }
            }
            persist(context, next, callback);
        });
    }

    public static void remove(Context context, String productId) {
        remove(context, productId, (success, errorMessage) -> {
        });
    }

    public static void remove(Context context, String productId, ResultCallback callback) {
        withLoaded(context, (success, errorMessage) -> {
            if (!success) {
                callback.onComplete(false, errorMessage);
                return;
            }

            List<CartEntry> next = copyItems();
            int index = findIndex(productId, next);
            if (index >= 0) {
                next.remove(index);
            }
            persist(context, next, callback);
        });
    }

    public static void clear(Context context) {
        clear(context, (success, errorMessage) -> {
        });
    }

    public static void clear(Context context, ResultCallback callback) {
        withLoaded(context, (success, errorMessage) -> {
            if (!success) {
                callback.onComplete(false, errorMessage);
                return;
            }
            persist(context, new ArrayList<>(), callback);
        });
    }

    public static synchronized boolean isInCart(Context context, String productId) {
        return findIndex(productId, ITEMS) >= 0;
    }

    public static synchronized int getTotalQuantity(Context context) {
        int total = 0;
        for (CartEntry item : ITEMS) {
            total += item.quantity;
        }
        return total;
    }

    public static synchronized double getTotalPrice(Context context) {
        double total = 0d;
        for (CartEntry item : ITEMS) {
            total += item.product.getPriceValue() * item.quantity;
        }
        return total;
    }

    public static synchronized String getTotalPriceText(Context context) {
        return String.format(Locale.getDefault(), "%.0f ₽", getTotalPrice(context));
    }

    public static synchronized List<CartEntry> getEntries(Context context) {
        return copyItems();
    }

    private static void withLoaded(Context context, ResultCallback callback) {
        boolean isLoaded;
        synchronized (CartManager.class) {
            isLoaded = loaded;
        }
        if (isLoaded) {
            callback.onComplete(true, null);
            return;
        }
        load(context, callback);
    }

    private static void persist(Context context, List<CartEntry> items, ResultCallback callback) {
        String authorization = AuthSession.getAuthorization(context);
        String userId = AuthSession.getUserId(context);
        if (TextUtils.isEmpty(authorization) || TextUtils.isEmpty(userId)) {
            callback.onComplete(false, "Нет данных пользователя");
            return;
        }

        if (items.isEmpty()) {
            if (TextUtils.isEmpty(basketId)) {
                synchronized (CartManager.class) {
                    clearCache();
                    loaded = true;
                }
                callback.onComplete(true, null);
                return;
            }

            ApiClient.getApiService().deleteBasket(authorization, basketId).enqueue(new retrofit2.Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    synchronized (CartManager.class) {
                        clearCache();
                        loaded = true;
                    }
                    callback.onComplete(response.isSuccessful(), response.isSuccessful() ? null : "Не удалось очистить корзину");
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    callback.onComplete(false, t.getMessage());
                }
            });
            return;
        }

        JsonObject body = new JsonObject();
        body.addProperty("user_id", userId);
        body.add("items", buildItemsJson(items));
        body.addProperty("count", totalCount(items));

        Call<JsonObject> call = TextUtils.isEmpty(basketId)
                ? ApiClient.getApiService().createBasket(authorization, body)
                : ApiClient.getApiService().updateBasket(authorization, basketId, body);

        call.enqueue(new retrofit2.Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                synchronized (CartManager.class) {
                    if (response.isSuccessful() && response.body() != null) {
                        updateCacheFromBasket(response.body());
                        loaded = true;
                        callback.onComplete(true, null);
                    } else {
                        callback.onComplete(false, "Не удалось сохранить корзину");
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onComplete(false, t.getMessage());
            }
        });
    }

    private static synchronized void updateCacheFromResponse(JsonObject response) {
        clearCache();
        if (!response.has("items") || !response.get("items").isJsonArray()) {
            return;
        }

        JsonArray list = response.getAsJsonArray("items");
        if (list.size() == 0 || !list.get(0).isJsonObject()) {
            return;
        }
        updateCacheFromBasket(list.get(0).getAsJsonObject());
    }

    private static synchronized void updateCacheFromBasket(JsonObject basket) {
        clearCache();
        basketId = getString(basket, "id");
        if (!basket.has("items") || !basket.get("items").isJsonArray()) {
            return;
        }

        JsonArray items = basket.getAsJsonArray("items");
        for (JsonElement element : items) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject item = element.getAsJsonObject();
            String productId = getString(item, "product_id");
            String title = getString(item, "title");
            double price = item.has("price") && !item.get("price").isJsonNull()
                    ? item.get("price").getAsDouble()
                    : 0d;
            int quantity = item.has("quantity") && !item.get("quantity").isJsonNull()
                    ? item.get("quantity").getAsInt()
                    : 0;

            CatalogProduct product = new CatalogProduct(
                    productId,
                    title,
                    "",
                    "",
                    String.format(Locale.getDefault(), "%.0f ₽", price),
                    price,
                    "",
                    ""
            );
            ITEMS.add(new CartEntry(product, quantity));
        }
    }

    private static JsonArray buildItemsJson(List<CartEntry> items) {
        JsonArray array = new JsonArray();
        for (CartEntry item : items) {
            JsonObject obj = new JsonObject();
            obj.addProperty("product_id", item.product.getId());
            obj.addProperty("title", item.product.getTitle());
            obj.addProperty("price", item.product.getPriceValue());
            obj.addProperty("quantity", item.quantity);
            array.add(obj);
        }
        return array;
    }

    private static synchronized List<CartEntry> copyItems() {
        List<CartEntry> copy = new ArrayList<>();
        for (CartEntry item : ITEMS) {
            copy.add(new CartEntry(item.product, item.quantity));
        }
        return copy;
    }

    private static int findIndex(String productId, List<CartEntry> items) {
        for (int i = 0; i < items.size(); i++) {
            if (productId.equals(items.get(i).product.getId())) {
                return i;
            }
        }
        return -1;
    }

    private static int totalCount(List<CartEntry> items) {
        int count = 0;
        for (CartEntry item : items) {
            count += item.quantity;
        }
        return count;
    }

    private static synchronized void clearCache() {
        basketId = null;
        ITEMS.clear();
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
