package com.okason.prontonotepad.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.okason.prontonotepad.model.Category;
import com.okason.prontonotepad.model.Note;
import com.okason.prontonotepad.util.Constants;

import java.util.ArrayList;
import java.util.List;


public class DeleteCategoryIntentService extends IntentService {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private DatabaseReference mDatabase;
    private DatabaseReference noteCloudReference;
    private DatabaseReference categoryCloudReference;
    private List<Category> mCategories;


    public DeleteCategoryIntentService() {
        super("DeleteCategoryIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mCategories = new ArrayList<>();

        noteCloudReference =  mDatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        categoryCloudReference =  mDatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.CATEGORY_CLOUD_END_POINT);

        categoryCloudReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot categorySnapshot: dataSnapshot.getChildren()){
                    Category category = null;
                    try {
                        category = categorySnapshot.getValue(Category.class);
                        mCategories.add(category);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        Bundle args = intent.getExtras();

        if (args != null && args.containsKey(Constants.SELECTED_CATEGORY_ID)){
            final String categoryId = args.getString(Constants.SELECTED_CATEGORY_ID);
            if (!TextUtils.isEmpty(categoryId)){
                noteCloudReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot noteSnapshot: dataSnapshot.getChildren()){
                            Note note = noteSnapshot.getValue(Note.class);
                            if (!TextUtils.isEmpty(note.getCategoryId()) && note.getCategoryId().equals(categoryId)){
                                String defaultCategoryId = getDefaultCategoryId();
                                note.setCategoryId(defaultCategoryId);
                                updateNoteBackToFirebase(note);
                            }
                        }
                        categoryCloudReference.child(categoryId).removeValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        }
    }

    private void updateNoteBackToFirebase(Note note) {
        noteCloudReference.child(note.getNoteId()).setValue(note);
    }

    private String getDefaultCategoryId() {
        Category category = null;
        for (Category cat: mCategories){
            if (cat.getCategoryName().equals(Constants.DEFAULT_CATEGORY)){
                category = cat;
                break;
            }
        }
        if (category == null){
            return  addCategoryToFirebase(Constants.DEFAULT_CATEGORY);
        }else {
            return category.getCategoryId();
        }

    }

    private String addCategoryToFirebase(String category) {
        Category cat = new Category();
        cat.setCategoryName(category);
        String key = categoryCloudReference.push().getKey();
        cat.setCategoryId(key);
        categoryCloudReference.child(key).setValue(cat);
        return cat.getCategoryId();
    }


}
