package com.example.sp01;

import android.content.Context;
import android.text.TextUtils;

import com.example.sp01.api.ApiClient;
import com.example.sp01.entity.ProjectItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class ProjectStorage {

    private ProjectStorage() {
    }

    public interface ProjectsCallback {
        void onComplete(List<ProjectItem> projects, String errorMessage);
    }

    public interface SaveCallback {
        void onComplete(boolean success, String errorMessage);
    }

    public static void getProjects(Context context, ProjectsCallback callback) {
        String authorization = AuthSession.getAuthorization(context);
        if (TextUtils.isEmpty(authorization)) {
            callback.onComplete(new ArrayList<>(), "Нет авторизации");
            return;
        }

        ApiClient.getApiService().getProjects(authorization, 1, 30).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onComplete(parseProjects(response.body()), null);
                } else {
                    callback.onComplete(new ArrayList<>(), "Не удалось загрузить проекты");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onComplete(new ArrayList<>(), t.getMessage());
            }
        });
    }

    public static void saveProject(Context context, String type, String title, String startDate,
                                   String endDate, String size, String descriptionSource,
                                   SaveCallback callback) {
        String authorization = AuthSession.getAuthorization(context);
        String userId = AuthSession.getUserId(context);
        if (TextUtils.isEmpty(authorization) || TextUtils.isEmpty(userId)) {
            callback.onComplete(false, "Нет данных пользователя");
            return;
        }

        ApiClient.getApiService().createProject(
                authorization,
                toPart(title),
                toPart(type),
                toPart(normalizeDate(startDate)),
                toPart(normalizeDate(endDate)),
                toPart(size),
                toPart(descriptionSource),
                null,
                toPart(userId)
        ).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                callback.onComplete(response.isSuccessful(), response.isSuccessful() ? null : "Не удалось создать проект");
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                callback.onComplete(false, t.getMessage());
            }
        });
    }

    private static List<ProjectItem> parseProjects(JsonObject response) {
        List<ProjectItem> result = new ArrayList<>();
        if (!response.has("items") || !response.get("items").isJsonArray()) {
            return result;
        }

        JsonArray items = response.getAsJsonArray("items");
        for (JsonElement element : items) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject item = element.getAsJsonObject();
            result.add(new ProjectItem(
                    getString(item, "id"),
                    getString(item, "type"),
                    getString(item, "title"),
                    getString(item, "date_start"),
                    getString(item, "date_end"),
                    getString(item, "size"),
                    getString(item, "description_source"),
                    getString(item, "technical_drawing"),
                    parseCreatedAt(getString(item, "created"))
            ));
        }

        return result;
    }

    private static RequestBody toPart(String value) {
        return RequestBody.create(value == null ? "" : value, MediaType.parse("text/plain"));
    }

    private static String normalizeDate(String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }
        if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return value;
        }
        if (value.matches("\\d{2}\\.\\d{2}\\.\\d{4}")) {
            return value.substring(6, 10) + "-" + value.substring(3, 5) + "-" + value.substring(0, 2);
        }
        return value;
    }

    private static long parseCreatedAt(String value) {
        if (TextUtils.isEmpty(value)) {
            return 0L;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            return format.parse(value).getTime();
        } catch (ParseException e) {
            return 0L;
        }
    }

    private static String getString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }
}
