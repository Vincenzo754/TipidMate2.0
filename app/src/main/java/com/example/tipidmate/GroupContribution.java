package com.example.tipidmate;

import java.io.Serializable;

public class GroupContribution implements Serializable {
    private String memberName;
    private double amount;
    private long timestamp;

    public GroupContribution(String memberName, double amount) {
        this.memberName = memberName;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    public String getMemberName() {
        return memberName;
    }

    public double getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
