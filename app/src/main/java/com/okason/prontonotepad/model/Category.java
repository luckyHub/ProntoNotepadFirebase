package com.okason.prontonotepad.model;

/**
 * Created by Tony on 5/16/2017.
 */

public class Category {
    private String categoryId;
    private String categoryName;
    private int count;

    public Category(String categoryId, String categoryName, int count) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.count = count;
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
