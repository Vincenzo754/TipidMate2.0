package com.example.tipidmate.models;

import com.google.gson.annotations.SerializedName;

public class Contribution {
    @SerializedName("contribution_id")
    private int contributionId;

    @SerializedName("goal_id")
    private int goalId;

    @SerializedName("amount")
    private double amount;

    @SerializedName("notes")
    private String notes;

    @SerializedName("contribution_date")
    private String contributionDate;

    // Empty constructor
    public Contribution() {
    }

    // Constructor for creating new contribution
    public Contribution(int goalId, double amount, String notes) {
        this.goalId = goalId;
        this.amount = amount;
        this.notes = notes;
    }

    // Getters
    public int getContributionId() { return contributionId; }
    public int getGoalId() { return goalId; }
    public double getAmount() { return amount; }
    public String getNotes() { return notes; }
    public String getContributionDate() { return contributionDate; }

    // Setters
    public void setContributionId(int contributionId) { this.contributionId = contributionId; }
    public void setGoalId(int goalId) { this.goalId = goalId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setContributionDate(String contributionDate) { this.contributionDate = contributionDate; }
}