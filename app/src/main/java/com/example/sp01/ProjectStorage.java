package com.example.sp01;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.sp01.entity.ProjectItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ProjectStorage {

    private static final String PREF_NAME = "projects_state";
    private static final String KEY_PROJECTS = "projects_json";

    private ProjectStorage() {
    }

    public static void saveProject(Context context, String type, String name, String startDate,
                                   String endDate, String projectFor, String source,
                                   String category) {
        List<ProjectItem> projects = getProjects(context);
        projects.add(0, new ProjectItem(
                UUID.randomUUID().toString(),
                type,
                name,
                startDate,
                endDate,
                projectFor,
                source,
                category,
                System.currentTimeMillis()
        ));
        persist(context, projects);
    }

    public static List<ProjectItem> getProjects(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String raw = preferences.getString(KEY_PROJECTS, "[]");
        List<ProjectItem> result = new ArrayList<>();

        try {
            JsonElement parsed = JsonParser.parseString(raw);
            if (!parsed.isJsonArray()) {
                return result;
            }

            JsonArray array = parsed.getAsJsonArray();
            for (JsonElement element : array) {
                if (!element.isJsonObject()) {
                    continue;
                }
                JsonObject obj = element.getAsJsonObject();
                result.add(new ProjectItem(
                        getString(obj, "id"),
                        getString(obj, "type"),
                        getString(obj, "name"),
                        getString(obj, "startDate"),
                        getString(obj, "endDate"),
                        getString(obj, "projectFor"),
                        getString(obj, "source"),
                        getString(obj, "category"),
                        obj.has("createdAt") ? obj.get("createdAt").getAsLong() : 0L
                ));
            }
        } catch (Exception ignored) {
        }

        return result;
    }

    private static void persist(Context context, List<ProjectItem> projects) {
        JsonArray array = new JsonArray();
        for (ProjectItem project : projects) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", project.getId());
            obj.addProperty("type", project.getType());
            obj.addProperty("name", project.getName());
            obj.addProperty("startDate", project.getStartDate());
            obj.addProperty("endDate", project.getEndDate());
            obj.addProperty("projectFor", project.getProjectFor());
            obj.addProperty("source", project.getSource());
            obj.addProperty("category", project.getCategory());
            obj.addProperty("createdAt", project.getCreatedAt());
            array.add(obj);
        }

        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_PROJECTS, array.toString())
                .apply();
    }

    private static String getString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : "";
    }
}
