package com.okason.prontonotepad.listeners;


import com.okason.prontonotepad.model.Category;

/**
 * Created by Valentine on 3/6/2016.
 */
public interface OnCategorySelectedListener {
    void onCategorySelected(Category selectedCategory);
    void onEditCategoryButtonClicked(Category selectedCategory);
    void onDeleteCategoryButtonClicked(Category selectedCategory);
}
