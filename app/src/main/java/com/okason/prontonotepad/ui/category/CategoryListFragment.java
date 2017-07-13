package com.okason.prontonotepad.ui.category;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.okason.prontonotepad.R;
import com.okason.prontonotepad.model.Category;
import com.okason.prontonotepad.model.Note;
import com.okason.prontonotepad.ui.listeners.OnCategorySelectedListener;
import com.okason.prontonotepad.util.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class CategoryListFragment extends Fragment implements OnCategorySelectedListener {

    private List<Note> mNotes;
    private List<Category> mCategories;
    private CategoryListAdapter mAdapter;
    private View mRootView;

    @BindView(R.id.category_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.empty_text)
    TextView mEmptyText;

    private DatabaseReference mDatabase;
    private DatabaseReference noteCloudReference;
    private DatabaseReference categoryCloudReference;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FloatingActionButton mFab;

    public CategoryListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_category_list, container, false);
        ButterKnife.bind(this, mRootView);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        noteCloudReference = mDatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        categoryCloudReference = mDatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.CATEGORY_CLOUD_END_POINT);

        mNotes = new ArrayList<>();
        mCategories = new ArrayList<>();

        //attach a Firebase Database listener to the Note cloud reference and Category cloud reference
        //as not using the FirebaseUI adapter for Categories

        noteCloudReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot noteSnapshot : dataSnapshot.getChildren()) {
                    Note note = noteSnapshot.getValue(Note.class);
                    mNotes.add(note);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        categoryCloudReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // load list of categories from data snapshot and handle recycler visibility ,
                // and category note count
                // and show any existing categories in the Recycler
                // by passing a list of  categories to the adapter replaceData method
                loadCategories(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        mAdapter = new CategoryListAdapter(getContext(), mCategories, this);
        mRecyclerView.setAdapter(mAdapter);

        return mRootView;

    }

    private void loadCategories(DataSnapshot dataSnapshot) {
        if (dataSnapshot != null) {
            mCategories.clear();
            for (DataSnapshot categorySnapshot : dataSnapshot.getChildren()) {
                Category category = null;
                try {
                    category = categorySnapshot.getValue(Category.class);
                    mCategories.add(category);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mAdapter.replaceData(mCategories);
        }

        if (mCategories.size() > 0) {
            hideEmptyText();

            for (Category category : mCategories) {
                category.setCount(getNoteCount(category.getCategoryId())); //get count per CategoryId
            }
            showCategories(mCategories);
        } else {
            showEmptyText();
        }

    }

    public void showCategories(List<Category> categories) {
        mAdapter.replaceData(categories);
    }

    public int getNoteCount(String categoryId) { //count noted in a category Id
        int count = 0;
        for (Note note : mNotes) {
            if (!TextUtils.isEmpty(note.getCategoryId())) {
                if (note.getCategoryId().equals(categoryId)) {
                    count++;
                }
            }
        }
        return count;
    }

    public void hideEmptyText() {   //"Empty text" message is diaplyed when no recycler
        mRecyclerView.setVisibility(View.VISIBLE);
        mEmptyText.setVisibility(View.GONE);
    }

    public void showEmptyText() {
        mRecyclerView.setVisibility(View.GONE);
        mEmptyText.setVisibility(View.VISIBLE);
    }


    //following methods are for

    @Override
    public void onCategorySelected(Category selectedCategory) {

    }

    @Override
    public void onEditCategoryButtonClicked(Category selectedCategory) {

    }

    @Override
    public void onDeleteCategoryButtonClicked(Category selectedCategory) {

    }
}


