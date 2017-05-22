package com.okason.prontonotepad;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.okason.prontonotepad.model.Note;
import com.okason.prontonotepad.model.SampleData;
import com.okason.prontonotepad.util.Constants;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private DatabaseReference mNoteCloudReference;
    private DatabaseReference mCategoryCloudReference;



    private String mUsername;
    private String mEmailAddress;
    private String mPhotoUrl;
    public static final String ANONYMOUS = "anonymous";
    public static final String ANONYMOUS_PHOTO_URL = "https://dl.dropboxusercontent.com/u/15447938/notepadapp/anon_user_48dp.png";
    public static final String ANONYMOUS_EMAIL = "anonymous@noemail.com";

    private AccountHeader mHeader = null;
    private Drawer mDrawer = null;
    private Activity mActivity;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(android.R.id.content)
    View mRootView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        mActivity = MainActivity.this;

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //reference to child nodes, Note and Category.
        mNoteCloudReference =  mDatabase.child(Constants.USERS_CLOUD_END_POINT
                + mFirebaseUser.getUid()
                + Constants.NOTE_CLOUD_END_POINT);
        mCategoryCloudReference =  mDatabase.child(Constants.USERS_CLOUD_END_POINT
                + mFirebaseUser.getUid()
                + Constants.CATEGORY_CLOUD_END_POINT);

        //check if user signed in
        if (mFirebaseUser == null) {
            startActivity(new Intent(MainActivity.this, AuthUiActivity.class));
            finish();
            return;
        } else {
            //user signed in
            mUsername = mFirebaseUser.getDisplayName();
            mEmailAddress = mFirebaseUser.getEmail();
            mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            String uid = mFirebaseUser.getUid();
        }

        setUpNavigationDrawer(savedInstanceState);

        FloatingActionButton floatingBtn = (FloatingActionButton) findViewById(R.id.fab);
        floatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v, "replace action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


    }

    protected void onResume() {
        super.onResume();
        addDataToFirebase();

    }

    public void  addDataToFirebase(){
        List<Note> notes = SampleData.getSampleNotes();
        for(Note note : notes) {
            String key = mNoteCloudReference.push().getKey();     ///get note key reference
            note.setNoteId(key);                                 /// give note class id of key
            mNoteCloudReference.child(key).setValue(note);       ///use cloud database reference to add child note class  at key location
        }





    }


    private void setUpNavigationDrawer(Bundle savedInstanceState) {
        mUsername = TextUtils.isEmpty(mUsername) ? ANONYMOUS : mUsername;
        mPhotoUrl = TextUtils.isEmpty(mPhotoUrl) ? ANONYMOUS_PHOTO_URL : mPhotoUrl;
        mEmailAddress = TextUtils.isEmpty(mEmailAddress) ? ANONYMOUS_EMAIL : mEmailAddress;

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
                .addDrawerItems(new PrimaryDrawerItem()
                                .withName("Notes")
                                .withIcon(GoogleMaterial.Icon.gmd_view_list)
                                .withIdentifier(Constants.NOTES),
                        new PrimaryDrawerItem().withName("Categories").withIcon(GoogleMaterial.Icon.gmd_folder).withIdentifier(Constants.CATEGORIES),
                        new PrimaryDrawerItem().withName("Settings").withIcon(GoogleMaterial.Icon.gmd_settings).withIdentifier(Constants.SETTINGS),
                        new PrimaryDrawerItem().withName("Logout").withIcon(GoogleMaterial.Icon.gmd_lock)
                                .withIdentifier(Constants.LOGOUT)
                )
                //add listener for item clicks
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //set title of toolbar
                        if (drawerItem != null && drawerItem instanceof Nameable) {
                            String name = ((Nameable) drawerItem).getName().getText(mActivity);
                            toolbar.setTitle(name);
                        }
                        if (drawerItem != null) {
                            onTouchDrawer((int) drawerItem.getIdentifier());
                        }
                        return false;
                    }
                })
                //drawer listener
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
        //add footer , outside of DrawerBuilder();
        mDrawer.addStickyFooterItem(new PrimaryDrawerItem()
                .withName("Delete Account!")
                .withIcon(GoogleMaterial.Icon.gmd_delete)
                .withIdentifier(Constants.DELETE));

    }

    private void onTouchDrawer(int position) {
        switch (position) {
            case Constants.NOTES:
                //Do Nothing, we are already on Notes
                break;
            case Constants.CATEGORIES:
                ///startActivity(new Intent(NoteListActivity.this, CategoryActivity.class));
                break;
            case Constants.SETTINGS:
                //Go to Settings
                ///startActivity(new Intent(NoteListActivity.this, SettingsActivity.class));
                break;
            case Constants.LOGOUT:
                logout();
                break;
            case Constants.DELETE:
                deleteAccountClicked();
                break;
        }

    }

    private void deleteAccountClicked() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Yes DO IT !", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAccount();
                    }
                })
                .setNegativeButton("No", null)
                .create();
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

    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }


}
