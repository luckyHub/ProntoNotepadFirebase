package com.okason.prontonotepad.ui.category;


import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.google.gson.Gson;
import com.okason.prontonotepad.R;
import com.okason.prontonotepad.listeners.OnCategorySelectedListener;
import com.okason.prontonotepad.model.Category;
import com.okason.prontonotepad.model.Note;
import com.okason.prontonotepad.util.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class CategoryListFragment extends Fragment implements OnCategorySelectedListener{

    private List<Note> mNotes;
    private List<Category> mCategories;
    private CategoryRecyclerViewAdapter mAdapter;
    private View mRootView;

    @BindView(R.id.category_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.empty_text)
    TextView mEmptyText;

    private AddCategoryDialogFragment addCategoryDialog;

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
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_category_list, container, false);
        ButterKnife.bind(this, mRootView);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        noteCloudReference =  mDatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        categoryCloudReference =  mDatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.CATEGORY_CLOUD_END_POINT);

        mNotes = new ArrayList<>();
        mCategories = new ArrayList<>();


        noteCloudReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot noteSnapshot: dataSnapshot.getChildren()){
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
                loadCategories(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




        mFab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddNewCategoryDialog();
            }
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAdapter = new CategoryRecyclerViewAdapter(getContext(),mCategories, this);
        mRecyclerView.setAdapter(mAdapter);

        return  mRootView;
    }

    private void loadCategories(DataSnapshot dataSnapshot) {
        if (dataSnapshot != null){
            mCategories.clear();
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

        if (mCategories.size() > 0){
            hideEmptyText();

            for (Category category: mCategories){
                category.setCount(getNoteCount(category.getCategoryId()));
            }
            showCategories(mCategories);
        }else {
            showEmptyText();
        }

    }

    public void showCategories(List<Category> categories) {
        mAdapter.replaceData(categories);
    }

    public void showEmptyText() {
        mRecyclerView.setVisibility(View.GONE);
        mEmptyText.setVisibility(View.VISIBLE);
    }

    public void hideEmptyText() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mEmptyText.setVisibility(View.GONE);
    }

    public void showAddNewCategoryDialog() {
        addCategoryDialog = AddCategoryDialogFragment.newInstance("");
        addCategoryDialog.show(getActivity().getFragmentManager(), "Dialog");
    }

    public void showConfirmDeleteCategoryPrompt(final Category category) {
        String title = getString(R.string.are_you_sure);
        String message =  getString(R.string.action_delete) + " " + category.getCategoryName();


        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View titleView = (View)inflater.inflate(R.layout.dialog_title, null);
        TextView titleText = (TextView)titleView.findViewById(R.id.text_view_dialog_title);
        titleText.setText(title);
        alertDialog.setCustomTitle(titleView);

        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               //Delete Category
                categoryCloudReference.child(category.getCategoryId()).removeValue();
            }
        });
        alertDialog.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();

    }

    public void showEditCategoryForm(Category category) {
        Gson gson = new Gson();
        String serializedCategory = gson.toJson(category);

        addCategoryDialog = AddCategoryDialogFragment.newInstance(serializedCategory);
        addCategoryDialog.show(getActivity().getFragmentManager(), "Dialog");
    }




    public int getNoteCount(String categoryId) {

        int count = 0;
        for (Note note: mNotes){
            if (!TextUtils.isEmpty(note.getCategoryId())) {
                if (note.getCategoryId().equals(categoryId)){
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public void onCategorySelected(Category selectedCategory) {

    }

    @Override
    public void onEditCategoryButtonClicked(Category selectedCategory) {
        showEditCategoryForm(selectedCategory);
    }

    @Override
    public void onDeleteCategoryButtonClicked(Category selectedCategory) {
        showConfirmDeleteCategoryPrompt(selectedCategory);
    }

    private void makeToast(String message){
        Snackbar snackbar = Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG);

        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary));
        TextView tv = (TextView)snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snackbar.show();
    }
}
