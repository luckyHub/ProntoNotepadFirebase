package com.okason.prontonotepad.ui.listeners;

import com.okason.prontonotepad.model.Category;

public interface OnCategorySelectedListener {

    void onCategorySelected(Category selectedCategory);
    void onEditCategoryButtonClicked(Category selectedCategory);
    void onDeleteCategoryButtonClicked(Category selectedCategory);

}
