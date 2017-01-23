package com.okason.prontonotepad.ui.notedetails;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.google.gson.Gson;
import com.okason.prontonotepad.R;
import com.okason.prontonotepad.listeners.OnEditNoteButtonClickedListener;
import com.okason.prontonotepad.model.Note;
import com.okason.prontonotepad.ui.addNote.AddNoteActivity;
import com.okason.prontonotepad.util.Constants;

public class NoteDetailActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Gson gson = new Gson();

        if (getIntent() != null && getIntent().hasExtra(Constants.SERIALIZED_NOTE)){
            String serializedNote = getIntent().getStringExtra(Constants.SERIALIZED_NOTE);
            Note passedInNote = gson.fromJson(serializedNote, Note.class);
            String title = passedInNote != null ? passedInNote.getTitle() : getString(R.string.note_detail);
            NoteDetailFragment fragment = NoteDetailFragment.newInstance(serializedNote);
            fragment.setmListener(new OnEditNoteButtonClickedListener() {
                @Override
                public void onEditNote(Note clickedNote) {
                    String serializedNote = gson.toJson(clickedNote);
                    Intent editNoteIntent = new Intent(NoteDetailActivity.this, AddNoteActivity.class);
                    editNoteIntent.putExtra(Constants.SERIALIZED_NOTE, serializedNote);
                    startActivity(editNoteIntent);
                }
            });

            openFragment(fragment, title);
        }else {
            finish();
        }
    }

    private void openFragment(Fragment fragment, String screenTitle){
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
        getSupportActionBar().setTitle(screenTitle);
    }

    public static Intent getStartIntent(final Context context, final String serializedNote) {
        Intent intent = new Intent(context, NoteDetailActivity.class);
        intent.putExtra(Constants.SERIALIZED_NOTE, serializedNote);
        return intent;
    }


}
