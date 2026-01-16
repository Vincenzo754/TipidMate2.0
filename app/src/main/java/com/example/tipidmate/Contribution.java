package com.example.tipidmate;

import java.io.Serializable;

public class Contribution implements Serializable {
    private final double amount;
    private final long timestamp;
    private final String note;

    public Contribution(double amount, long timestamp, String note) {
        this.amount = amount;
        this.timestamp = timestamp;
        this.note = note;
    }

    public double getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getNote() {
        return note;
    }
}
