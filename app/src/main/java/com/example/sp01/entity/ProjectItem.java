package com.example.sp01.entity;

public class ProjectItem {

    private final String id;
    private final String type;
    private final String name;
    private final String startDate;
    private final String endDate;
    private final String projectFor;
    private final String source;
    private final String category;
    private final long createdAt;

    public ProjectItem(String id, String type, String name, String startDate, String endDate,
                       String projectFor, String source, String category, long createdAt) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.projectFor = projectFor;
        this.source = source;
        this.category = category;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getProjectFor() {
        return projectFor;
    }

    public String getSource() {
        return source;
    }

    public String getCategory() {
        return category;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
