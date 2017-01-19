package com.okason.prontonotepad.addNote;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.okason.prontonotepad.R;
import com.okason.prontonotepad.util.Constants;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoteEditorFragment extends Fragment {


    public NoteEditorFragment() {
        // Required empty public constructor
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

}
