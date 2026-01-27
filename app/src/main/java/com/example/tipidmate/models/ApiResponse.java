package com.example.tipidmate.models;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("user")
    private User user;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("expense_id")
    private int expenseId;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public User getUser() { return user; }
    public int getUserId() { return userId; }
    public int getExpenseId() { return expenseId; }
}
