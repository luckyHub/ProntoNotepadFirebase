package com.okason.prontonotepad.ui.notes;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.okason.prontonotepad.R;
import com.okason.prontonotepad.model.Note;
import com.okason.prontonotepad.ui.addnote.AddNoteActivitiy;
import com.okason.prontonotepad.util.Constants;
import com.okason.prontonotepad.util.TimeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

//#import com.okason.prontonotepad.ui.notedetails.NoteDetailActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoteListFragment extends Fragment {

    private DatabaseReference mDatabase;
    private DatabaseReference noteCloudReference;
    private DatabaseReference categoryCloudReference;
    private boolean isDualScreen = false;

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FloatingActionButton mFab;
    private View mRootView;
    private FirebaseRecyclerAdapter<Note, NoteViewHolder> mNoteFirebaseAdapter;

    @BindView(R.id.note_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.empty_text)
    TextView mEmptyText;

    public NoteListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);    //flag to parent

    }

    public static NoteListFragment newInstance(boolean dualScreen) {
        NoteListFragment fragment = new NoteListFragment();
        Bundle args = new Bundle();
        args.putBoolean(Constants.IS_DUAL_SCREEN, dualScreen);
        fragment.setArguments(args);
        return fragment;
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

        //#String sortColumn = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(getString(R.string.column_title), getString(R.string.column_title));

        noteCloudReference = mDatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        categoryCloudReference = mDatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.CATEGORY_CLOUD_END_POINT);

        //#Query noteCloudQuery =  noteCloudReference.orderByChild(sortColumn);

        //Recycler and Adapter
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mNoteFirebaseAdapter = new FirebaseRecyclerAdapter<Note, com.okason.prontonotepad.ui.notes.NoteViewHolder>(
                Note.class,
                R.layout.row_note_list,
                com.okason.prontonotepad.ui.notes.NoteViewHolder.class,
                //#        noteCloudReference.orderByChild(sortColumn)) {
                noteCloudReference) {

            @Override
            protected Note parseSnapshot(DataSnapshot snapshot) {
                Note note = super.parseSnapshot(snapshot);
                if (note != null) {
                    note.setNoteId(snapshot.getKey());
                }
                return note;
            }

            @Override
            protected void populateViewHolder(com.okason.prontonotepad.ui.notes.NoteViewHolder holder, final Note note, int position) {
                holder.title.setText(note.getTitle());
                holder.title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Gson gson = new Gson();
                        String serializedNoteObject = gson.toJson(note);
                        Intent editIntent = new Intent(getActivity(), AddNoteActivitiy.class);
                        editIntent.putExtra(Constants.SERIALIZED_NOTE, serializedNoteObject);
                        startActivity(editIntent); //Fragment cannot start a fragment

                    }
                });

                holder.noteDate.setText(TimeUtils.getDueDate(note.getDateModified()));
                holder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        promptForDelete(note);
                    }
                });

                try {
                    if (note.getNoteType().equals(Constants.NOTE_TYPE_AUDIO)) {
                        Glide.with(getContext()).load(R.drawable.headphone_button).into(holder.noteCircleIcon);
                    } else if (note.getNoteType().equals(Constants.NOTE_TYPE_REMINDER)) {
                        Glide.with(getContext()).load(R.drawable.appointment_reminder).into(holder.noteCircleIcon);
                    } else if (note.getNoteType().equals(Constants.NOTE_TYPE_IMAGE)) {
                        //Show the image
                    } else {                   //Show TextView Image

                        String firstLetter = note.getTitle().substring(0, 1);
                        ColorGenerator generator = ColorGenerator.MATERIAL;
                        int color = generator.getRandomColor();

                        holder.noteCircleIcon.setVisibility(View.GONE);
                        holder.noteIcon.setVisibility(View.VISIBLE);

                        TextDrawable drawable = TextDrawable.builder()
                                .buildRound(firstLetter, color);
                        holder.noteIcon.setImageDrawable(drawable);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        };

        mNoteFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                int noteCount = mNoteFirebaseAdapter.getItemCount();
                if (noteCount > 0) {
                    hideEmptyText();
                } else {
                    showEmptyText();
                }
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                int noteCount = mNoteFirebaseAdapter.getItemCount();
                if (noteCount > 0) {
                    hideEmptyText();
                } else {
                    showEmptyText();
                }
            }
        });

        mRecyclerView.setAdapter(mNoteFirebaseAdapter);

        return mRootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_edit:
                break;


        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onResume() {
        super.onResume();
        Bundle args = getArguments();
        if (args != null && args.containsKey(Constants.IS_DUAL_SCREEN)) {
            isDualScreen = args.getBoolean(Constants.IS_DUAL_SCREEN);
        }
        showEmptyText();
    }

//#   public void openNoteDetails(Note note) {
//        if (isDualScreen) {
//            showDualDetailUi(note);
//        } else {
//            showSingleDetailUi(note);
//        }
//    }

//#   public void showDualDetailUi(Note note) {
//        NoteListActivity activity = (NoteListActivity) getActivity();
//        activity.showTwoPane(note);
//
//    }

//#    public void showSingleDetailUi(Note note) {
//        Gson gson = new Gson();
//        String serializedNote = gson.toJson(note);
//        startActivity(NoteDetailActivity.getStartIntent(getContext(), serializedNote));
//    }

    public void promptForDelete(final Note note) {

        String content = note.getContent();
        String message = "Delete " + content.substring(0, Math.min(content.length(), 50)) + "  ... ?";


        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View titleView = (View) inflater.inflate(R.layout.dialog_title, null);
        TextView titleText = (TextView) titleView.findViewById(R.id.text_view_dialog_title);
        titleText.setText(getString(R.string.are_you_sure));
        alertDialog.setCustomTitle(titleView);

        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TextUtils.isEmpty(note.getNoteId())) {
                    noteCloudReference.child(note.getNoteId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (mNoteFirebaseAdapter.getItemCount() < 1) {
                                showEmptyText();
                            }
                        }
                    });
                }
            }
        });
        alertDialog.setNegativeButton(getString(R.string.action_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }


    public void showEmptyText() {
        mRecyclerView.setVisibility(View.GONE);
        mEmptyText.setVisibility(View.VISIBLE);
    }

    public void hideEmptyText() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mEmptyText.setVisibility(View.GONE);
    }


}
