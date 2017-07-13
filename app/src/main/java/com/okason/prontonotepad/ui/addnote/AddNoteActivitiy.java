package com.okason.prontonotepad.ui.addnote;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.okason.prontonotepad.R;
import com.okason.prontonotepad.util.Constants;

public class AddNoteActivitiy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note_activitiy);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (getIntent() != null && getIntent().hasExtra(Constants.SERIALIZED_NOTE)){
            String serializedNote = getIntent().getStringExtra(Constants.SERIALIZED_NOTE);
            openFragment(NoteEditorFragment.newInstance(serializedNote), getString(R.string.note_editor));
        }else {
            openFragment(NoteEditorFragment.newInstance(""), getString(R.string.note_editor));
        }

    }

    private void openFragment(Fragment fragment, String title){
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.container,fragment)
                .addToBackStack(title)
                .commit();

        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
            //additional code
        } else {
            getFragmentManager().popBackStack();  //manage Backstack
        }
    }

}
