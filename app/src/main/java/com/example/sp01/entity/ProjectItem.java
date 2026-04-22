package com.example.sp01.entity;

public class ProjectItem {

    private final String id;
    private final String type;
    private final String name;
    private final String startDate;
    private final String endDate;
    private final String size;
    private final String source;
    private final String technicalDrawing;
    private final long createdAt;

    public ProjectItem(String id, String type, String name, String startDate, String endDate,
                       String size, String source, String technicalDrawing, long createdAt) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.size = size;
        this.source = source;
        this.technicalDrawing = technicalDrawing;
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

    public String getSize() {
        return size;
    }

    public String getSource() {
        return source;
    }

    public String getTechnicalDrawing() {
        return technicalDrawing;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
