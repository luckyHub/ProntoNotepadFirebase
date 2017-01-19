package com.okason.prontonotepad.ui.category;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.okason.prontonotepad.R;
import com.okason.prontonotepad.model.Category;

import java.util.List;

/**
 * Created by Valentine on 2/17/2016.
 */
public class CategoryAdapter extends ArrayAdapter<Category> {
    private List<Category> mCategories;
    private Category activeCategory;
    private Context mContext;


    public Category getActiveCategory() {
        return activeCategory;
    }

    public CategoryAdapter(Context context, List<Category> categories){
        super(context, android.R.layout.simple_list_item_1, categories);
        mCategories = categories;
        mContext = context;

    }

    @Override
    public int getCount() {
        return mCategories.size();
    }


    @Override
    public Category getItem(int position) {
        if (position < mCategories.size()) {
            return mCategories.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Category category = mCategories.get(position);



        View view = LayoutInflater.from(getContext()).inflate(R.layout.category_list_text, null);
        TextView text1 = (TextView) view.findViewById(android.R.id.text1);
        text1.setText(category.getCategoryName());

        //check if there is current category and highlight it in the list
        if (activeCategory != null){
            if (category.equals(activeCategory)){
                text1.setTextColor(ContextCompat.getColor(mContext, R.color.primary));
            }
        }

        return view;
    }


}
