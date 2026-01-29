package com.example.tipidmate.models;

import com.google.gson.annotations.SerializedName;

public class Budget {
    @SerializedName("budget_id")
    private int budgetId;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("amount")
    private double amount;

    @SerializedName("frequency")
    private String frequency; // "monthly" or "daily"

    @SerializedName("start_date")
    private String startDate;

    @SerializedName("end_date")
    private String endDate;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("spent")
    private double spent;

    @SerializedName("remaining")
    private double remaining;

    @SerializedName("percentage")
    private double percentage;

    @SerializedName("created_at")
    private String createdAt;

    // Constructor for creating new budget
    public Budget(int userId, double amount, String frequency) {
        this.userId = userId;
        this.amount = amount;
        this.frequency = frequency;
    }

    // Getters
    public int getBudgetId() { return budgetId; }
    public int getUserId() { return userId; }
    public double getAmount() { return amount; }
    public String getFrequency() { return frequency; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public boolean isActive() { return isActive; }
    public double getSpent() { return spent; }
    public double getRemaining() { return remaining; }
    public double getPercentage() { return percentage; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setBudgetId(int budgetId) { this.budgetId = budgetId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public void setActive(boolean active) { isActive = active; }
    public void setSpent(double spent) { this.spent = spent; }
    public void setRemaining(double remaining) { this.remaining = remaining; }
    public void setPercentage(double percentage) { this.percentage = percentage; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}