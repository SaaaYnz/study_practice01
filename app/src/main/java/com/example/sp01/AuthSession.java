package com.example.sp01;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public final class AuthSession {

    private AuthSession() {
    }

    public static String getAuthorization(Context context) {
        String token = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                .getString("access_token", null);
        return TextUtils.isEmpty(token) ? null : "Bearer " + token;
    }

    public static String getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        if (!TextUtils.isEmpty(userId)) {
            return userId;
        }

        String token = prefs.getString("access_token", null);
        if (TextUtils.isEmpty(token)) {
            return null;
        }

        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            return null;
        }

        try {
            byte[] decoded = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            JSONObject json = new JSONObject(new String(decoded, StandardCharsets.UTF_8));
            if (json.has("id")) {
                return json.optString("id", null);
            }
            if (json.has("sub")) {
                return json.optString("sub", null);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    public static void saveAuth(Context context, String token, String email, String name, String userId) {
        context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                .edit()
                .putString("access_token", token)
                .putString("user_email", email)
                .putString("user_name", name)
                .putString("user_id", userId)
                .apply();
    }
}
