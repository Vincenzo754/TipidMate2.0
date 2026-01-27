package com.example.tipidmate.models;

import com.google.gson.annotations.SerializedName;

public class GroupBudgetMember {
    @SerializedName("member_id")
    private int memberId;

    @SerializedName("group_budget_id")
    private int groupBudgetId;

    @SerializedName("member_name")
    private String memberName;

    @SerializedName("initials")
    private String initials;

    @SerializedName("total_contributed")
    private double totalContributed;

    @SerializedName("contribution_count")
    private int contributionCount;

    // Empty constructor
    public GroupBudgetMember() {
    }

    // Constructor for creating new member
    public GroupBudgetMember(int groupBudgetId, String memberName) {
        this.groupBudgetId = groupBudgetId;
        this.memberName = memberName;
    }

    // Getters
    public int getMemberId() { return memberId; }
    public int getGroupBudgetId() { return groupBudgetId; }
    public String getMemberName() { return memberName; }
    public String getInitials() { return initials; }
    public double getTotalContributed() { return totalContributed; }
    public int getContributionCount() { return contributionCount; }

    // Setters
    public void setMemberId(int memberId) { this.memberId = memberId; }
    public void setGroupBudgetId(int groupBudgetId) { this.groupBudgetId = groupBudgetId; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public void setInitials(String initials) { this.initials = initials; }
}