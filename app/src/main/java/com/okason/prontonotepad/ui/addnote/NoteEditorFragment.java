package com.okason.prontonotepad.ui.addnote;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.okason.prontonotepad.MainActivity;
import com.okason.prontonotepad.R;
import com.okason.prontonotepad.model.Note;
import com.okason.prontonotepad.util.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoteEditorFragment extends Fragment {
    @BindView(R.id.edit_text_category)
    EditText mCategory;
    @BindView(R.id.edit_text_title)
    EditText mTitle;
    @BindView(R.id.edit_text_note)
    EditText mContent;
    @BindView(R.id.image_attachment)
    ImageView mImageAttachment;
    @BindView(R.id.sketch_attachment)
    ImageView mSketchAttachment;

    private View mRootView;
    private Note mCurrentNote;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mNoteReference;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private boolean isInEditMode = false;

    public NoteEditorFragment() {
        // Required empty public constructor --- see newInstance method belwo
    }

    public static NoteEditorFragment newInstance(String contents) {
        NoteEditorFragment fragment = new NoteEditorFragment();  // new editor fragment
        if (!TextUtils.isEmpty(contents)) {               // indicates edit of existing note
            Bundle args = new Bundle();
            args.putString("serialized_note", contents);   //add note contents as String
            fragment.setArguments(args);
        }
        return fragment;             // if new editor fragment , no args
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //Firebase refs
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mNoteReference = mDatabaseReference.child(Constants.USERS_CLOUD_END_POINT
                + mFirebaseUser.getUid()
                + Constants.NOTE_CLOUD_END_POINT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_note_editor, container, false);
        ButterKnife.bind(this, mRootView);
        getCurrentNote();
        return mRootView;
    }

    //deserialize String into Note object w/ GSON
    public void getCurrentNote() {
        Bundle args = getArguments();
        if (args != null && args.containsKey(Constants.SERIALIZED_NOTE)) {
            String serializedNote = args.getString(Constants.SERIALIZED_NOTE, "");
            if (!serializedNote.isEmpty()) {
                Gson gson = new Gson();
                mCurrentNote = gson.fromJson(serializedNote, new TypeToken<Note>() {
                }.getType());
                if (mCurrentNote != null & !TextUtils.isEmpty(mCurrentNote.getNoteId())) {
                    isInEditMode = true;
                }
            }
        }
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

//        if (!TextUtils.isEmpty(note.getLocalAudioPath())){
//            mLocalAudioFilePath = note.getLocalAudioPath();
//            audioUploadedToCloud = note.isCloudAudioExists();
//        }
//        if (!TextUtils.isEmpty(note.getLocalImagePath())){
//            mLocalImagePath = note.getLocalImagePath();
//            imageUploadedToCloud = note.isCloudImageExists();
//            populateImage(mLocalImagePath, imageUploadedToCloud);
//        }
//        if (!TextUtils.isEmpty(note.getLocalSketchImagePath())){
//            mLocalSketchPath = note.getLocalSketchImagePath();
//            sketchUploadedToCloud = note.isCloudSketchExists();
//            populateSketch(mLocalImagePath, sketchUploadedToCloud);
//        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_note, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (validateContent()) {
                    addNoteToFirebase("");
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addNoteToFirebase(String s) {
        if (mCurrentNote == null) { //new note
            mCurrentNote = new Note(); //create client side object
            String key = mNoteReference.push().getKey(); //push returns fb database location
            mCurrentNote.setNoteId(key);
        }

        mCurrentNote.setContent(mContent.getText().toString());
        mCurrentNote.setTitle(mTitle.getText().toString());
        mCurrentNote.setDateModified(System.currentTimeMillis());
        //WRITE client note to cloud
        mNoteReference.child(mCurrentNote.getNoteId()).setValue(mCurrentNote);
        makeSnackbar("Note created/updated");
        startActivity(new Intent(getActivity(), MainActivity.class)); //return to Activity
    }

    private boolean validateContent() {
        String title = mTitle.getText().toString();
        if (TextUtils.isEmpty(title)) {
            mTitle.setError(getString(R.string.title_is_required));
            return false;
        }

        String content = mContent.getText().toString();
        if (TextUtils.isEmpty(content)) {
            mContent.setError(getString(R.string.note_is_required));
            return false;
        }
        return true;
    }

    private void makeSnackbar(String message){
        Snackbar snackbar = Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG);

        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary));
        TextView tv = (TextView)snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snackbar.show();
    }
}

