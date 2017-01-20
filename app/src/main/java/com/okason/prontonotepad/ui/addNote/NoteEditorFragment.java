package com.okason.prontonotepad.ui.addNote;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.okason.prontonotepad.R;
import com.okason.prontonotepad.listeners.OnCategorySelectedListener;
import com.okason.prontonotepad.model.Category;
import com.okason.prontonotepad.model.Note;
import com.okason.prontonotepad.ui.category.SelectCategoryDialogFragment;
import com.okason.prontonotepad.util.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.R.attr.category;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoteEditorFragment extends Fragment {

    private boolean mEditMode;
    private Note mCurrentNote = null;
    private Category mCurrentCategory = null;
    private View mRootView;

    private List<Category> mCategories;
    private SelectCategoryDialogFragment selectCategoryDialog;


    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private DatabaseReference mDatabase;
    private DatabaseReference noteCloudReference;
    private DatabaseReference categoryCloudReference;
    private DatabaseReference noteCategoryCloudReference;

    @BindView(R.id.edit_text_category) EditText mCategory;
    @BindView(R.id.edit_text_title) EditText mTitle;
    @BindView(R.id.edit_text_note) EditText mContent;


    public NoteEditorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public static NoteEditorFragment newInstance(long noteId){
        NoteEditorFragment fragment = new NoteEditorFragment();
        if (noteId > 0){
            Bundle args = new Bundle();
            args.putLong(Constants.NOTE_ID, noteId);
            fragment.setArguments(args);
        }
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_note_editor, container, false);
        ButterKnife.bind(this, mRootView);

        mCategories = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        noteCloudReference =  mDatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        categoryCloudReference =  mDatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.CATEGORY_CLOUD_END_POINT);


        categoryCloudReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null){
                    for (DataSnapshot categorySnapshot: dataSnapshot.getChildren()){
                        Category category = categorySnapshot.getValue(Category.class);
                        mCategories.add(category);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return mRootView;
    }

    @OnClick(R.id.edit_text_category)
    public void showSelectCategory(){
        showChooseCategoryDialog(mCategories);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_note, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                validateAndSaveContent();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    private void validateAndSaveContent() {

        if (mCurrentCategory == null) {
            mCategory.setText(Constants.DEFAULT_CATEGORY);
            addCategoryToFirebase(Constants.DEFAULT_CATEGORY);
            makeToast("Default Category added");
        }

        String title = mTitle.getText().toString();
        if (TextUtils.isEmpty(title)) {
            mTitle.setError(getString(R.string.title_is_required));
            return;
        }

        String content = mContent.getText().toString();
        if (TextUtils.isEmpty(content)) {
            mContent.setError(getString(R.string.note_is_required));
            return;
        }

        addNoteToFirebase();

    }

    private void addCategoryToFirebase(String category) {
        Category cat = new Category();
        cat.setCategoryName(category);
        String key = categoryCloudReference.push().getKey();
        cat.setCategoryId(key);
        categoryCloudReference.child(key).setValue(cat);
        mCurrentCategory = cat;
    }

    private void addNoteToFirebase() {

        Note note = new Note();
        note.setNoteType(Constants.NOTE_TYPE_TEXT);
        if (mCurrentCategory != null) {
            note.setCategoryId(mCurrentCategory.getCategoryId());
        }
        note.setContent(mContent.getText().toString());
        note.setTitle(mTitle.getText().toString());

        String key = noteCloudReference.push().getKey();
        note.setNoteId(key);
        noteCloudReference.child(key).setValue(note);




        makeToast("Note added");
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();

    }


    private void makeToast(String message){
        Snackbar snackbar = Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG);

        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary));
        TextView tv = (TextView)snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public void showChooseCategoryDialog(List<Category> categories) {
        selectCategoryDialog = SelectCategoryDialogFragment.newInstance();
        selectCategoryDialog.setCategories(categories);

        selectCategoryDialog.setCategorySelectedListener(new OnCategorySelectedListener() {
            @Override
            public void onCategorySelected(Category selectedCategory) {
                selectCategoryDialog.dismiss();
                mCategory.setText(selectedCategory.getCategoryName());
                mCurrentCategory = selectedCategory;
            }

            @Override
            public void onEditCategoryButtonClicked(Category selectedCategory) {

            }

            @Override
            public void onDeleteCategoryButtonClicked(Category selectedCategory) {

            }
        });
        selectCategoryDialog.show(getActivity().getFragmentManager(), "Dialog");
    }

}
