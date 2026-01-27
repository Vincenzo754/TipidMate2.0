package com.example.tipidmate.models;

import com.google.gson.annotations.SerializedName;

public class GroupBudget {
    @SerializedName("group_budget_id")
    private int groupBudgetId;

    @SerializedName("creator_user_id")
    private int creatorUserId;

    @SerializedName("budget_name")
    private String budgetName;

    @SerializedName("description")
    private String description;

    @SerializedName("target_amount")
    private double targetAmount;

    @SerializedName("current_amount")
    private double currentAmount;

    @SerializedName("budget_type")
    private String budgetType;

    @SerializedName("member_count")
    private int memberCount;

    @SerializedName("created_at")
    private String createdAt;

    // Empty constructor
    public GroupBudget() {
    }

    // Constructor for creating new group budget
    public GroupBudget(int creatorUserId, String budgetName, String description,
                       double targetAmount, String budgetType) {
        this.creatorUserId = creatorUserId;
        this.budgetName = budgetName;
        this.description = description;
        this.targetAmount = targetAmount;
        this.budgetType = budgetType;
    }

    // Getters
    public int getGroupBudgetId() { return groupBudgetId; }
    public int getCreatorUserId() { return creatorUserId; }
    public String getBudgetName() { return budgetName; }
    public String getDescription() { return description; }
    public double getTargetAmount() { return targetAmount; }
    public double getCurrentAmount() { return currentAmount; }
    public String getBudgetType() { return budgetType; }
    public int getMemberCount() { return memberCount; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setGroupBudgetId(int groupBudgetId) { this.groupBudgetId = groupBudgetId; }
    public void setCreatorUserId(int creatorUserId) { this.creatorUserId = creatorUserId; }
    public void setBudgetName(String budgetName) { this.budgetName = budgetName; }
    public void setDescription(String description) { this.description = description; }
    public void setTargetAmount(double targetAmount) { this.targetAmount = targetAmount; }
    public void setCurrentAmount(double currentAmount) { this.currentAmount = currentAmount; }
    public void setBudgetType(String budgetType) { this.budgetType = budgetType; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
}