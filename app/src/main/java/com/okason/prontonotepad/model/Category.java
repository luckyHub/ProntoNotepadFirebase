package com.okason.prontonotepad.model;

public class Category {
    private String categoryId;
    private String categoryName;
    private int count;

    public Category() {
    }

    public Category(String id, String name) {
        this.categoryId = id;
        this.categoryName = name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
