package com.example.tipidmate.models;

import com.google.gson.annotations.SerializedName;

public class Expense {
    @SerializedName("expense_id")
    private int expenseId;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("category_id")
    private int categoryId;

    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("amount")
    private double amount;

    @SerializedName("description")
    private String description;

    @SerializedName("expense_date")
    private String expenseDate;

    public Expense(int userId, int categoryId, double amount, String description, String expenseDate) {
        this.userId = userId;
        this.categoryId = categoryId;
        this.amount = amount;
        this.description = description;
        this.expenseDate = expenseDate;
    }

    public int getExpenseId() { return expenseId; }
    public int getUserId() { return userId; }
    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public double getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getExpenseDate() { return expenseDate; }
}