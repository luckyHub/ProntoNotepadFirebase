package com.okason.prontonotepad.ui.category;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.okason.prontonotepad.R;
import com.okason.prontonotepad.model.Category;
import com.okason.prontonotepad.util.Constants;


/**
 * Created by Tony on 7/11/2017.
 */

public class AddCategoryDialogFragment extends DialogFragment {

    private EditText mCategoryEditText;
    private boolean mInEditMode = false;   //to distinguish add from edit
    private Category mCategory;

    private DatabaseReference categoryCloudReference;

    public AddCategoryDialogFragment() {
        //required empty constructor
    }

    /**
     * The method gets the Category that was passed in, in the form of serialized String
     * if nothing was passed in then it will create a new Category
     * I
     */
    public void getCurrentCategory() {
        Bundle args = getArguments();
        if (args != null && args.containsKey(Constants.SERIALZED_CATEGORY)) {
            String serializedCategory = args.getString(Constants.SERIALIZED_CATEGORY, "");
            if (!TextUtils.isEmpty(serializedCategory)) {
                Gson gson = new Gson();
                mCategory = gson.fromJson(serializedCategory, new TypeToken<Category>() {
                }.getType());
                if (mCategory != null & !TextUtils.isEmpty(mCategory.getCategoryId())) {
                    mInEditMode = true;
                }
            }
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final AlertDialog.Builder addCategoryDialog = new AlertDialog.Builder(getActivity());

        if (savedInstanceState == null) {  //i.e.  First Time
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View convertView = inflater.inflate(R.layout.fragment_add_category_dialog, null);
            addCategoryDialog.setView(convertView);

            getCurrentCategory();

            //inflate title to provide custom text for title
            View titleView = (View) inflater.inflate(R.layout.dialog_title, null);
            TextView titleText = (TextView) titleView.findViewById(R.id.text_view_dialog_title);
            titleText.setText(mInEditMode == true ? getString(R.string.edit_category) : getString(R.string.add_category));
            addCategoryDialog.setCustomTitle(titleView);

            mCategoryEditText = (EditText) convertView.findViewById(R.id.edit_text_add_category);


            addCategoryDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });
            addCategoryDialog.setPositiveButton(mInEditMode == true ? "Update" : "Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // click ok button will dismiss dialog regardless
                    // cannot check for condition where no text entered
                    // have to override fragment onStart() to get dialog and check field completed

                }
            });


            if (mInEditMode) {
                populateFields(mCategory);
                addCategoryDialog.setTitle(mCategory.getCategoryName());
            }


        }

        return addCategoryDialog.create();

    }


    private void populateFields(Category category) {
        mCategoryEditText.setText(category.getCategoryName());
    }


    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog)getDialog();


        if (d != null){
            Button positiveButton = (Button)d.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean readyToCloseDialog = false;
                    if (requiredFieldCompleted()) {
                        saveCategory();
                        readyToCloseDialog = true;
                    }
                    if (readyToCloseDialog)
                        dismiss();
                }
            });
        }
    }

    private boolean requiredFieldCompleted() {
        if (mCategoryEditText.getText().toString().isEmpty()) {
            mCategoryEditText.setError(getString(R.string.category_name_is_required));
            mCategoryEditText.requestFocus();
            return false;


        }
        return true;
    }

    private void saveCategory() {
        if (mInEditMode){
            if (mCategory != null){
                mCategory.setCategoryName(mCategoryEditText.getText().toString().trim());
                //Update to Firebase
                categoryCloudReference.child(mCategory.getCategoryId()).setValue(mCategory);
            }
        }else {
            Category category = new Category();
            category.setCategoryName(mCategoryEditText.getText().toString().trim());
            category.setCategoryId(categoryCloudReference.push().getKey());
            categoryCloudReference.child(category.getCategoryId()).setValue(category);
        }

    }

}