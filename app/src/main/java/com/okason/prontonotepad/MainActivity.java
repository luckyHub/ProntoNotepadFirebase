package com.okason.prontonotepad;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.mikepenz.materialdrawer.util.KeyboardUtil;
import com.okason.prontonotepad.auth.AuthUiActivity;
import com.okason.prontonotepad.listeners.OnEditNoteButtonClickedListener;
import com.okason.prontonotepad.model.Category;
import com.okason.prontonotepad.model.Note;
import com.okason.prontonotepad.model.SampleData;
import com.okason.prontonotepad.ui.addNote.AddNoteActivity;
import com.okason.prontonotepad.ui.addNote.NoteEditorFragment;
import com.okason.prontonotepad.ui.category.CategoryActivity;
import com.okason.prontonotepad.ui.notedetails.NoteDetailFragment;
import com.okason.prontonotepad.ui.notes.NoteListFragment;
import com.okason.prontonotepad.util.Constants;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private  FloatingActionButton fab;
    private AccountHeader mHeader = null;
    private Drawer mDrawer = null;
    private Activity mActivity;
    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;




    @BindView(android.R.id.content)  View mRootView;
    @BindView(R.id.toolbar) Toolbar toolbar;

    public static final String ANONYMOUS = "anonymous";
    public static final String ANONYMOUS_PHOTO_URL = "https://dl.dropboxusercontent.com/u/15447938/notepadapp/anon_user_48dp.png";
    public static final String ANONYMOUS_EMAIL = "anonymous@noemail.com";
    private static final String LOG_TAG = "MainActivity";
    private String mUsername;
    private String mPhotoUrl;
    private String mEmailAddress;


    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private DatabaseReference noteCloudReference;
    private DatabaseReference categoryCloudReference;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActivity = this;

        if (findViewById(R.id.note_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w800dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            findViewById(R.id.note_detail_container).setVisibility(View.GONE);
        }




        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();



        if (mFirebaseUser == null){
            //Not signed in, launch the Sign In Activity
            startActivity(new Intent(this, AuthUiActivity.class));
            finish();
            return;
        }else {
            mUsername = mFirebaseUser.getDisplayName();
            mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            mEmailAddress = mFirebaseUser.getEmail();
        }

        setupNavigationDrawer(savedInstanceState);

        noteCloudReference =  mDatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        categoryCloudReference =  mDatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.CATEGORY_CLOUD_END_POINT);



        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mActivity, AddNoteActivity.class));
            }
        });

        addDefaultData();
        openFragment(NoteListFragment.newInstance(mTwoPane), "Notes");
    }



    private void setupNavigationDrawer(Bundle savedInstanceState) {
        mUsername = TextUtils.isEmpty(mUsername) ? ANONYMOUS : mUsername;
        mEmailAddress = TextUtils.isEmpty(mEmailAddress) ? ANONYMOUS_EMAIL : mEmailAddress;
        mPhotoUrl = TextUtils.isEmpty(mPhotoUrl) ? ANONYMOUS_PHOTO_URL : mPhotoUrl;

        IProfile profile = new ProfileDrawerItem()
                .withName(mUsername)
                .withEmail("someemail@gymmail.com")
                .withIcon(mPhotoUrl)
                .withIdentifier(102);

        mHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header_2)
                .addProfiles(profile)
                .build();
        mDrawer = new DrawerBuilder()
                .withAccountHeader(mHeader)
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Notes").withIcon(GoogleMaterial.Icon.gmd_view_list).withIdentifier(Constants.NOTES),
                        new PrimaryDrawerItem().withName("Categories").withIcon(GoogleMaterial.Icon.gmd_folder).withIdentifier(Constants.CATEGORIES),
                        new PrimaryDrawerItem().withName("Settings").withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(Constants.SETTINGS),
                        new PrimaryDrawerItem().withName("Logout").withIcon(GoogleMaterial.Icon.gmd_lock).withIdentifier(Constants.LOGOUT)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem != null && drawerItem instanceof Nameable){
                            String name = ((Nameable) drawerItem).getName().getText(mActivity);
                            toolbar.setTitle(name);
                        }

                        if (drawerItem != null){
                            //handle on navigation drawer item
                            onTouchDrawer((int) drawerItem.getIdentifier());
                        }
                        return false;
                    }
                })
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        KeyboardUtil.hideKeyboard(MainActivity.this);
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {

                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {

                    }
                })
                .withFireOnInitialOnClick(true)
                .withSavedInstance(savedInstanceState)
                .build();
        mDrawer.addStickyFooterItem(new PrimaryDrawerItem().withName("Delete Account!").withIcon(GoogleMaterial.Icon.gmd_delete).withIdentifier(Constants.DELETE));


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void onTouchDrawer(int position) {
        switch (position){
            case Constants.NOTES:
                //Do Nothing, we are already on Notes
                break;
            case Constants.CATEGORIES:
                startActivity(new Intent(MainActivity.this, CategoryActivity.class));
                //Go to Tags
                break;
            case Constants.SETTINGS:
                //Go to Settings
                break;
            case Constants.LOGOUT:
                logout();
                break;
            case Constants.DELETE:
                deleteAccountClicked();
                break;
        }

    }

    private void logout() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(mActivity, MainActivity.class));
                            finish();
                        } else {
                            showSnackbar(R.string.sign_out_failed);
                        }
                    }
                });

    }

    public void deleteAccountClicked() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Yes, nuke it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteAccount();
                    }
                })
                .setNegativeButton("No", null)
                .create();

        dialog.show();
    }

    private void deleteAccount() {
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(mActivity, MainActivity.class));
                            finish();
                        } else {
                            showSnackbar(R.string.delete_account_failed);
                        }
                    }
                });
    }


    private void addInitialNotesToFirebase() {

        List<Note> sampleNotes = SampleData.getSampleNotes();
        for (Note note: sampleNotes){
            String key = noteCloudReference.push().getKey();
            note.setNoteId(key);
            noteCloudReference.child(key).setValue(note);
        }

    }

    private void addInitialCategoriesToFirebase(){
        List<String> categoryNames = SampleData.getSampleCategories();
        for (String categoryName: categoryNames){
            Category category = new Category();
            category.setCategoryName(categoryName);
            category.setCategoryId(categoryCloudReference.push().getKey());
            categoryCloudReference.child(category.getCategoryId()).setValue(category);
        }

    }




    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

    private void openFragment(Fragment fragment, String screenTitle){
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.container, fragment)
                .addToBackStack(screenTitle)
                .commit();
        getSupportActionBar().setTitle(screenTitle);
    }

    private void openDetailFragment(Fragment fragment, String screenTitle){
        getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.note_detail_container, fragment)
                .addToBackStack(null)
                .commit();
        getSupportActionBar().setTitle(screenTitle);
    }

    private void addDefaultData() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        editor = sharedPreferences.edit();
        if (sharedPreferences.getBoolean(Constants.FIRST_RUN, true)) {
            addInitialNotesToFirebase();
            addInitialCategoriesToFirebase();
            editor.putBoolean(Constants.FIRST_RUN, false).commit();
        }
    }

    public void showTwoPane(Note note){
        findViewById(R.id.note_detail_container).setVisibility(View.VISIBLE);
        Gson gson = new Gson();
        String serializedNote = gson.toJson(note);
        String title = note != null ? note.getTitle() : getString(R.string.note_detail);
        NoteDetailFragment fragment = NoteDetailFragment.newInstance(serializedNote);
        fragment.setmListener(new OnEditNoteButtonClickedListener() {
            @Override
            public void onEditNote(Note clickedNote) {
                if (clickedNote != null){
                    Gson gson = new Gson();
                    String serializedNote = gson.toJson(clickedNote);
                    String title = TextUtils.isEmpty(clickedNote.getTitle()) ?  getString(R.string.note_editor): clickedNote.getTitle();
                    openDetailFragment(NoteEditorFragment.newInstance(serializedNote), title);
                }
            }
        });
        openDetailFragment(fragment, title);
    }





}
