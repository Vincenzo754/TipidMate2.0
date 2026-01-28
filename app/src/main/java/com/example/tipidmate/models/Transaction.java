package com.example.tipidmate.models;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("transaction_id")
    private int transactionId;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("type")
    private String type;

    @SerializedName("category")
    private String category;

    @SerializedName("amount")
    private double amount;

    @SerializedName("note")
    private String note;

    @SerializedName("transaction_date")
    private String transactionDate;

    @SerializedName("transaction_time")
    private String transactionTime;

    @SerializedName("created_at")
    private String createdAt;

    // Constructor for creating new transactions
    public Transaction(int userId, String type, String category, double amount, String note,
                       String transactionDate, String transactionTime) {
        this.userId = userId;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.note = note;
        this.transactionDate = transactionDate;
        this.transactionTime = transactionTime;
    }

    // Getters
    public int getTransactionId() { return transactionId; }
    public int getUserId() { return userId; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getNote() { return note; }
    public String getTransactionDate() { return transactionDate; }
    public String getTransactionTime() { return transactionTime; }
    public String getCreatedAt() { return createdAt; }

    // Setters
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setType(String type) { this.type = type; }
    public void setCategory(String category) { this.category = category; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setNote(String note) { this.note = note; }
    public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }
    public void setTransactionTime(String transactionTime) { this.transactionTime = transactionTime; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}