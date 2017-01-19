package com.okason.prontonotepad.notes;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.okason.prontonotepad.R;
import com.okason.prontonotepad.model.Note;
import com.okason.prontonotepad.util.TimeUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoteListFragment extends Fragment {

    private DatabaseReference mDatabase;
    private DatabaseReference userSpecificRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FloatingActionButton mFab;
    private NotesAdapter mListAdapter;
    private FirebaseRecyclerAdapter<Note, NoteViewHolder> mNoteFirebaseAdapter;
    private View mRootView;

    @BindView(R.id.note_recycler_view) RecyclerView mRecyclerView;




    public NoteListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mListAdapter = new NotesAdapter(new ArrayList<Note>());
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_note_list, container, false);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_note_list, container, false);
        ButterKnife.bind(this, mRootView);


        userSpecificRef =  mDatabase.child("users/" + mFirebaseUser.getUid());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mNoteFirebaseAdapter = new FirebaseRecyclerAdapter<Note, NoteViewHolder>(
                Note.class,
                R.layout.row_note_list,
                NoteViewHolder.class,
                userSpecificRef.child("notes")) {

            @Override
            protected Note parseSnapshot(DataSnapshot snapshot) {
                Note note = super.parseSnapshot(snapshot);
                if (note != null){
                    note.setNoteId(snapshot.getKey());
                }
                return note;
            }

            @Override
            protected void populateViewHolder(NoteViewHolder holder, Note note, int position) {

                holder.title.setText(note.getTitle());
                holder.noteDate.setText(TimeUtils.getTimeAgo(note.getDateModified()));

            }
        };

        mNoteFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
            }
        });

        mRecyclerView.setAdapter(mNoteFirebaseAdapter);


        return mRootView;
    }






}
