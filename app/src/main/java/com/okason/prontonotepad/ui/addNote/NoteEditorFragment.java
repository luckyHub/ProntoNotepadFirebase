package com.okason.prontonotepad.ui.addNote;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.okason.prontonotepad.R;
import com.okason.prontonotepad.util.Constants;

import butterknife.BindView;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoteEditorFragment extends Fragment {

    @BindView(R.id.edit_text_tag) EditText mTagName;
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
        return inflater.inflate(R.layout.fragment_note_editor, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_note, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

}
