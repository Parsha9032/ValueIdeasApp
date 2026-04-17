package com.valueadd.app.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.valueadd.app.utils.Converters;

import java.util.Date;
import java.util.List;

@Entity(tableName = "ideas")
@TypeConverters(Converters.class)
public class Idea {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;
    private Date dateCreated;
    private Date dateModified;
    private List<String> tags;
    private String status; // "Not Started", "In Progress", "Completed"
    private int progressPercentage; // 0-100
    private boolean isPriority;
    private String milestone1;
    private String milestone2;
    private String milestone3;
    private boolean milestone1Done;
    private boolean milestone2Done;
    private boolean milestone3Done;

    public Idea() {
        this.dateCreated = new Date();
        this.dateModified = new Date();
        this.status = "Not Started";
        this.progressPercentage = 0;
        this.isPriority = false;
        this.milestone1Done = false;
        this.milestone2Done = false;
        this.milestone3Done = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getDateCreated() { return dateCreated; }
    public void setDateCreated(Date dateCreated) { this.dateCreated = dateCreated; }

    public Date getDateModified() { return dateModified; }
    public void setDateModified(Date dateModified) { this.dateModified = dateModified; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getProgressPercentage() { return progressPercentage; }
    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = Math.max(0, Math.min(100, progressPercentage));
    }

    public boolean isPriority() { return isPriority; }
    public void setPriority(boolean priority) { isPriority = priority; }

    public String getMilestone1() { return milestone1; }
    public void setMilestone1(String milestone1) { this.milestone1 = milestone1; }

    public String getMilestone2() { return milestone2; }
    public void setMilestone2(String milestone2) { this.milestone2 = milestone2; }

    public String getMilestone3() { return milestone3; }
    public void setMilestone3(String milestone3) { this.milestone3 = milestone3; }

    public boolean isMilestone1Done() { return milestone1Done; }
    public void setMilestone1Done(boolean milestone1Done) { this.milestone1Done = milestone1Done; }

    public boolean isMilestone2Done() { return milestone2Done; }
    public void setMilestone2Done(boolean milestone2Done) { this.milestone2Done = milestone2Done; }

    public boolean isMilestone3Done() { return milestone3Done; }
    public void setMilestone3Done(boolean milestone3Done) { this.milestone3Done = milestone3Done; }

    public String getTagsAsString() {
        if (tags == null || tags.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tags.size(); i++) {
            sb.append(tags.get(i));
            if (i < tags.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    public int getStatusColor() {
        if (status == null) return 0xFF9E9E9E;
        switch (status) {
            case "In Progress": return 0xFF2196F3;
            case "Completed": return 0xFF4CAF50;
            default: return 0xFF9E9E9E;
        }
    }
}
