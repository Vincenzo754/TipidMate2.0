package com.example.tipidmate;

public class Transaction {
    String title;
    long timestamp;
    double amount;
    String type; // "Income" or "Expense"
    int iconResId;
    int iconTint;

    public Transaction(String title, long timestamp, double amount, String type, int iconResId, int iconTint) {
        this.title = title;
        this.timestamp = timestamp;
        this.amount = amount;
        this.type = type;
        this.iconResId = iconResId;
        this.iconTint = iconTint;
    }
}
