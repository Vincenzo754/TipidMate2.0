package com.example.tipidmate.models;

import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Goal {
    @SerializedName("goal_id")
    private int goalId;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("goal_name")
    private String goalName;

    @SerializedName("description")
    private String description;

    @SerializedName("target_amount")
    private double targetAmount;

    @SerializedName("current_amount")
    private double currentAmount;

    @SerializedName("target_date")
    private String targetDate;

    @SerializedName("icon_name")
    private String iconName;

    @SerializedName("creation_date")
    private String creationDate;

    // Empty constructor for Gson
    public Goal() {
    }

    // Constructor for creating new goal
    public Goal(int userId, String goalName, String description, double targetAmount,
                double currentAmount, String targetDate, String iconName) {
        this.userId = userId;
        this.goalName = goalName;
        this.description = description;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.targetDate = targetDate;
        this.iconName = iconName;
    }

    // Getters
    public int getGoalId() { return goalId; }
    public int getUserId() { return userId; }
    public String getGoalName() { return goalName; }
    public String getDescription() { return description; }
    public double getTargetAmount() { return targetAmount; }
    public double getCurrentAmount() { return currentAmount; }
    public String getTargetDate() { return targetDate; }
    public String getIconName() { return iconName; }
    public String getCreationDate() { return creationDate; }

    public long getTimestamp() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(creationDate);
            return date.getTime();
        } catch (ParseException e) {
            return 0;
        }
    }

    // Setters
    public void setGoalId(int goalId) { this.goalId = goalId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setGoalName(String goalName) { this.goalName = goalName; }
    public void setDescription(String description) { this.description = description; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }
    public void setTargetDate(String targetDate) { this.targetDate = targetDate; }
    public void setIconName(String iconName) { this.iconName = iconName; }
    public void setCreationDate(String creationDate) { this.creationDate = creationDate; }
}
