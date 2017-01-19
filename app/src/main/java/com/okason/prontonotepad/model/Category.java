package com.okason.prontonotepad.model;

/**
 * Created by Valentine on 9/7/2015.
 */
public class Category {

    private String categoryId;
    private String categoryName;
    private String categoryColor;
    private int count;



    public Category() {
    }

    public Category(String id, String name){
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

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
