package com.example.tipidmate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Goal implements Serializable {
    private final String id;
    private String title;
    private String category;
    private double targetAmount;
    private double currentAmount;
    private long targetDate;
    private String description;
    private int iconResId;
    private List<Contribution> contributions;

    public Goal(String title, String category, double targetAmount, long targetDate, String description, int iconResId) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.category = category;
        this.targetAmount = targetAmount;
        this.targetDate = targetDate;
        this.description = description;
        this.iconResId = iconResId;
        this.currentAmount = 0;
        this.contributions = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void addSavings(Contribution contribution) {
        this.contributions.add(contribution);
        this.currentAmount += contribution.getAmount();
    }

    public void removeContribution(Contribution contribution) {
        this.contributions.remove(contribution);
        this.currentAmount -= contribution.getAmount();
    }

    public long getTargetDate() {
        return targetDate;
    }

    public String getDescription() {
        return description;
    }

    public int getIconResId() {
        return iconResId;
    }

    public List<Contribution> getContributions() {
        return contributions;
    }
}
