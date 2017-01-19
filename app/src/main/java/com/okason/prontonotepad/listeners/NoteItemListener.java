package com.okason.prontonotepad.listeners;


import com.okason.prontonotepad.model.Note;

/**
 * Created by Valentine on 3/12/2016.
 */
public interface NoteItemListener {

    void onNoteClick(Note clickedNote);

    void onDeleteButtonClicked(Note clickedNote);
}
