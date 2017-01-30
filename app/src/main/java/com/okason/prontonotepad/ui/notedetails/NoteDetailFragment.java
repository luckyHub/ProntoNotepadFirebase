package com.okason.prontonotepad.ui.notedetails;


import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.okason.prontonotepad.R;
import com.okason.prontonotepad.listeners.OnEditNoteButtonClickedListener;
import com.okason.prontonotepad.model.Note;
import com.okason.prontonotepad.util.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoteDetailFragment extends Fragment {

    private Menu menu;

    @BindView(R.id.edit_text_category)
    EditText mCategory;

    @BindView(R.id.edit_text_title)
    EditText mTitle;

    @BindView(R.id.edit_text_note)
    EditText mContent;

    private View mRootView;
    private Note mCurrentNote = null;
    private Toolbar mToolbarBottom;
    private OnEditNoteButtonClickedListener mListener;


    public NoteDetailFragment() {
        // Required empty public constructor
    }


    public static NoteDetailFragment newInstance(String serializedNote){
        NoteDetailFragment fragment = new NoteDetailFragment();
        Bundle arguments = new Bundle();
        arguments.putString(Constants.SERIALIZED_NOTE, serializedNote);
        fragment.setArguments(arguments);
        return fragment;
    }

    public void getCurrentNote(){
        Bundle args = getArguments();
        if (args != null && args.containsKey(Constants.SERIALIZED_NOTE)){
            String serializedNote = args.getString(Constants.SERIALIZED_NOTE);
            if (!TextUtils.isEmpty(serializedNote)){
                Gson gson = new Gson();
                mCurrentNote = gson.fromJson(serializedNote, Note.class);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getCurrentNote();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_note_editor, container, false);
        ButterKnife.bind(this, mRootView);
        displayReadOnlyViews();

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCurrentNote != null){
            displayNote(mCurrentNote);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_note_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_edit:
                if (mListener != null){
                    mListener.onEditNote(mCurrentNote);
                }
                break;


        }
        return super.onOptionsItemSelected(item);
    }

    public void displayReadOnlyViews() {
        mCategory.setFocusable(false);
        mTitle.setFocusable(false);
        mContent.setFocusable(false);
    }

    public void displayNote(Note note) {
        mCategory.setText(note.getCategoryName());
        mContent.setText(note.getContent());
        mTitle.setText(note.getTitle());

    }

    private void makeToast(String message){
        Snackbar snackbar = Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG);

        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary));
        TextView tv = (TextView)snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snackbar.show();
    }


    public OnEditNoteButtonClickedListener getmListener() {
        return mListener;
    }

    public void setmListener(OnEditNoteButtonClickedListener mListener) {
        this.mListener = mListener;
    }


}
