package com.example.tipidmate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GroupBudget implements Serializable {
    private final String id;
    private String title;
    private String description;
    private double targetAmount;
    private List<String> members;
    private List<GroupContribution> contributions = new ArrayList<>();
    private long timestamp;

    public GroupBudget(String title, String description, double targetAmount, List<String> members) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.targetAmount = targetAmount;
        this.members = members;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public List<String> getMembers() {
        return members;
    }

    public List<GroupContribution> getContributions() {
        return contributions;
    }

    public double getCurrentAmount() {
        double total = 0;
        for (GroupContribution contribution : contributions) {
            total += contribution.getAmount();
        }
        return total;
    }

    public double getMemberContribution(String memberName) {
        double total = 0;
        for (GroupContribution contribution : contributions) {
            if (contribution.getMemberName().equals(memberName)) {
                total += contribution.getAmount();
            }
        }
        return total;
    }

    public void removeMember(String member) {
        members.remove(member);
    }

    public void addContribution(GroupContribution contribution) {
        contributions.add(contribution);
    }

    public void removeContribution(GroupContribution contribution) {
        contributions.remove(contribution);
    }

    public long getTimestamp() {
        return timestamp;
    }
}
