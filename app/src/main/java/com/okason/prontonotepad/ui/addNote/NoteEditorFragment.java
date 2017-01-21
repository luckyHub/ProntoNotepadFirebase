package com.okason.prontonotepad.ui.addNote;


import android.content.Intent;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.okason.prontonotepad.MainActivity;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class NoteEditorFragment extends Fragment {

    private boolean isInEditMode;
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


    @BindView(R.id.edit_text_category) EditText mCategory;
    @BindView(R.id.edit_text_title) EditText mTitle;
    @BindView(R.id.edit_text_note) EditText mContent;


    public NoteEditorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getCurrentNote();
        setHasOptionsMenu(true);
    }

    public static NoteEditorFragment newInstance(String content){
        NoteEditorFragment fragment = new NoteEditorFragment();
        if (!TextUtils.isEmpty(content)){
            Bundle args = new Bundle();
            args.putString(Constants.SERIALIZED_NOTE, content);
            fragment.setArguments(args);
        }
        return fragment;
    }

    public void getCurrentNote(){
        Bundle args = getArguments();
        if (args != null && args.containsKey(Constants.SERIALIZED_NOTE)){
            String serializedNote = args.getString(Constants.SERIALIZED_NOTE, "");
            if (!serializedNote.isEmpty()){
                Gson gson = new Gson();
                mCurrentNote = gson.fromJson(serializedNote, new TypeToken<Note>(){}.getType());
                if (mCurrentNote != null & !TextUtils.isEmpty(mCurrentNote.getNoteId())){
                    isInEditMode = true;
                }
            }
        }
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


    @Override
    public void onResume() {
        super.onResume();
        if (isInEditMode){
            populateNote(mCurrentNote);
        }
    }

    public void populateNote(Note note) {
        mTitle.setText(note.getTitle());
        mTitle.setHint(R.string.placeholder_note_title);
        mContent.setText(note.getContent());
        mContent.setHint(R.string.placeholder_note_text);
        if (!TextUtils.isEmpty(note.getCategoryName())){
            mCategory.setText(note.getCategoryName());
        }else {
            mCategory.setText(Constants.DEFAULT_CATEGORY);
        }

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

    private void addNoteToFirebase() {
        if (isInEditMode && mCurrentNote != null){
            mCurrentNote.setContent(mContent.getText().toString());
            mCurrentNote.setTitle(mTitle.getText().toString());
            if (mCurrentCategory != null) {
                mCurrentNote.setCategoryName(mCurrentCategory.getCategoryName());
                mCurrentNote.setCategoryId(mCurrentCategory.getCategoryId());
            }else {
                mCurrentNote.setCategoryName(Constants.DEFAULT_CATEGORY);
                mCurrentNote.setCategoryId(getCategoryId(Constants.DEFAULT_CATEGORY));
            }
            mCurrentNote.setDateModified(System.currentTimeMillis());
            noteCloudReference.child(mCurrentNote.getNoteId()).setValue(mCurrentNote);
            makeToast("Note updated");

        } else {
            Note note = new Note();
            note.setNoteType(Constants.NOTE_TYPE_TEXT);
            if (mCurrentCategory != null) {
                note.setCategoryName(mCurrentCategory.getCategoryName());
                note.setCategoryId(mCurrentCategory.getCategoryId());
            }else {
                note.setCategoryName(Constants.DEFAULT_CATEGORY);
                note.setCategoryId(getCategoryId(Constants.DEFAULT_CATEGORY));
            }
            note.setContent(mContent.getText().toString());
            note.setTitle(mTitle.getText().toString());
            String key = noteCloudReference.push().getKey();
            note.setNoteId(key);
            note.setDateCreated(System.currentTimeMillis());
            note.setDateModified(System.currentTimeMillis());
            noteCloudReference.child(key).setValue(note);
            makeToast("Note added");
        }
        startActivity(new Intent(getActivity(), MainActivity.class));

    }

    private String getCategoryId(String categoryName) {
        for (Category category: mCategories){
            if (!TextUtils.isEmpty(category.getCategoryId()) && category.getCategoryName().equals(categoryName)){
                return category.getCategoryId();
            }
        }
        return addCategoryToFirebase(categoryName);
    }


    private String addCategoryToFirebase(String category) {
        Category cat = new Category();
        cat.setCategoryName(category);
        String key = categoryCloudReference.push().getKey();
        cat.setCategoryId(key);
        categoryCloudReference.child(key).setValue(cat);
        return key;
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
