package com.example.tipidmate.models;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("category_id")
    private int categoryId;

    @SerializedName("category_name")
    private String categoryName;

    @SerializedName("icon_name")
    private String iconName;

    public int getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getIconName() { return iconName; }
}