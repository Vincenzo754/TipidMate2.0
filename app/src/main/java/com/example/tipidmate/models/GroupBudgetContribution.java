package com.example.tipidmate.models;

import com.google.gson.annotations.SerializedName;

public class GroupBudgetContribution {
    @SerializedName("contribution_id")
    private int contributionId;

    @SerializedName("group_budget_id")
    private int groupBudgetId;

    @SerializedName("member_id")
    private int memberId;

    @SerializedName("member_name")
    private String memberName;

    @SerializedName("initials")
    private String initials;

    @SerializedName("amount")
    private double amount;

    @SerializedName("contribution_date")
    private String contributionDate;

    // Empty constructor
    public GroupBudgetContribution() {
    }

    // Constructor for creating new contribution
    public GroupBudgetContribution(int groupBudgetId, int memberId, double amount) {
        this.groupBudgetId = groupBudgetId;
        this.memberId = memberId;
        this.amount = amount;
    }

    // Getters
    public int getContributionId() { return contributionId; }
    public int getGroupBudgetId() { return groupBudgetId; }
    public int getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public String getInitials() { return initials; }
    public double getAmount() { return amount; }
    public String getContributionDate() { return contributionDate; }

    // Setters
    public void setContributionId(int contributionId) { this.contributionId = contributionId; }
    public void setGroupBudgetId(int groupBudgetId) { this.groupBudgetId = groupBudgetId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }
    public void setAmount(double amount) { this.amount = amount; }
}