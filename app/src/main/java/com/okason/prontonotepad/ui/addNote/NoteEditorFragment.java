package com.okason.prontonotepad.ui.addNote;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.okason.prontonotepad.MainActivity;
import com.okason.prontonotepad.R;
import com.okason.prontonotepad.listeners.OnCategorySelectedListener;
import com.okason.prontonotepad.model.Attachment;
import com.okason.prontonotepad.model.Category;
import com.okason.prontonotepad.model.Note;
import com.okason.prontonotepad.ui.category.SelectCategoryDialogFragment;
import com.okason.prontonotepad.util.Constants;
import com.okason.prontonotepad.util.FileUtils;
import com.okason.prontonotepad.util.TimeUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoteEditorFragment extends Fragment {

    private boolean isInEditMode;
    private Note mCurrentNote = null;
    private Category mCurrentCategory = null;
    private View mRootView;
    private Toolbar mToolbarBottom;

    private List<Category> mCategories;
    private List<Attachment> mAttachments;
    private SelectCategoryDialogFragment selectCategoryDialog;
    private final static String LOG_TAG = "NoteEditorFragment";


    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mFirebaseStorageReference;
    private StorageReference mImageStorageReference;
    private StorageReference mAudioStorageReference;


    private DatabaseReference mDatabase;
    private DatabaseReference noteCloudReference;
    private DatabaseReference categoryCloudReference;

    private final int EXTERNAL_PERMISSION_REQUEST = 1;
    private final int RECORD_AUDIO_PERMISSION_REQUEST = 2;
    private final int IMAGE_CAPTURE_PERMISSION_REQUEST = 3;


    @BindView(R.id.edit_text_category) EditText mCategory;
    @BindView(R.id.edit_text_title) EditText mTitle;
    @BindView(R.id.edit_text_note) EditText mContent;
    @BindView(R.id.image_attachment) ImageView mImageAttachment;

    private MediaRecorder mRecorder = null;
    private MediaPlayer   mPlayer = null;

    private String mLocalAudioFilePath = null;
    private boolean audioUploadedToCloud = false;
    private String mLocalImagePath = null;
    private boolean imageUploadedToCloud = false;

    private Uri mImageURI = null;



    public NoteEditorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getCurrentNote();
        setHasOptionsMenu(true);
    }

    public static NoteEditorFragment newInstance(String content){
        NoteEditorFragment fragment = new NoteEditorFragment();
        if (!TextUtils.isEmpty(content)){
            Bundle args = new Bundle();
            args.putString(Constants.SERIALIZED_NOTE, content);
            fragment.setArguments(args);
        }
        return fragment;
    }

    public void getCurrentNote(){
        Bundle args = getArguments();
        if (args != null && args.containsKey(Constants.SERIALIZED_NOTE)){
            String serializedNote = args.getString(Constants.SERIALIZED_NOTE, "");
            if (!serializedNote.isEmpty()){
                Gson gson = new Gson();
                mCurrentNote = gson.fromJson(serializedNote, new TypeToken<Note>(){}.getType());
                if (mCurrentNote != null & !TextUtils.isEmpty(mCurrentNote.getNoteId())){
                    isInEditMode = true;
                }
            }
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView = inflater.inflate(R.layout.fragment_note_editor, container, false);
        ButterKnife.bind(this, mRootView);

        mCategories = new ArrayList<>();
        mAttachments = new ArrayList<>();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseStorageReference = mFirebaseStorage.getReferenceFromUrl(Constants.FIREBASE_STORAGE_BUCKET);

        mAudioStorageReference = mFirebaseStorageReference.child(Constants.STORAGE_CLOUD_END_POINT_AUDIO);
        mImageStorageReference = mFirebaseStorageReference.child(Constants.STORAGE_CLOUD_END_POINT_IMAGES);

        noteCloudReference =  mDatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.NOTE_CLOUD_END_POINT);
        categoryCloudReference =  mDatabase.child(Constants.USERS_CLOUD_END_POINT + mFirebaseUser.getUid() + Constants.CATEGORY_CLOUD_END_POINT);


        categoryCloudReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null){
                    for (DataSnapshot categorySnapshot: dataSnapshot.getChildren()){
                        Category category = categorySnapshot.getValue(Category.class);
                        mCategories.add(category);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mToolbarBottom = (Toolbar)getActivity().findViewById(R.id.toolbar_bottom);
        mToolbarBottom.getMenu().clear();
        mToolbarBottom.inflateMenu(R.menu.menu_note_editor_bottom);


        mToolbarBottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                PackageManager packageManager = getActivity().getPackageManager();
                switch (id) {
                    case R.id.action_delete:
                        if (isInEditMode && mCurrentNote != null){
                            promptForDelete(mCurrentNote);
                        }else {
                            promptForDiscard();
                        }
                        break;
                    case R.id.action_share:
                        displayShareIntent();
                        break;
                    case R.id.action_camera:
                       //show camera intent
                        if (packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
                            if (isStoragePermissionGranted()) {
                                if (isRecordPermissionGranted()) {
                                    takePhoto();
                                }
                            }
                        } else {
                            makeToast(getContext().getString(R.string.error_no_camera));
                        }
                        break;
                    case R.id.action_record:
                        if (packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)) {
                            if (isStoragePermissionGranted()) {
                                if (isRecordPermissionGranted()) {
                                    promptToStartRecording();
                                }
                            }
                        } else {
                            makeToast(getContext().getString(R.string.error_no_mic));
                        }
                        break;
                    case R.id.action_play:
                        if (mLocalAudioFilePath == null){
                            makeToast("No Recording found");
                        }else{
                            startPlaying();
                        }

                }
                return true;
            }
        });

        return mRootView;
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

        if (!TextUtils.isEmpty(note.getLocalAudioPath())){
            mLocalAudioFilePath = note.getLocalAudioPath();
            audioUploadedToCloud = note.isCloudAudioExists();
        }
        if (!TextUtils.isEmpty(note.getLocalImagePath())){
            mLocalImagePath = note.getLocalImagePath();
            imageUploadedToCloud = note.isCloudImageExists();
        }


    }



    @OnClick(R.id.edit_text_category)
    public void showSelectCategory(){
        showChooseCategoryDialog(mCategories);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_note, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                validateAndSaveContent();
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    private void validateAndSaveContent() {

        String title = mTitle.getText().toString();
        if (TextUtils.isEmpty(title)) {
            mTitle.setError(getString(R.string.title_is_required));
            return;
        }

        String content = mContent.getText().toString();
        if (TextUtils.isEmpty(content)) {
            mContent.setError(getString(R.string.note_is_required));
            return;
        }
        addNoteToFirebase();

    }

    private void addNoteToFirebase() {
        if (isInEditMode && mCurrentNote != null){
            mCurrentNote.setContent(mContent.getText().toString());
            mCurrentNote.setTitle(mTitle.getText().toString());
            if (mCurrentCategory != null) {
                mCurrentNote.setCategoryName(mCurrentCategory.getCategoryName());
                mCurrentNote.setCategoryId(mCurrentCategory.getCategoryId());
            }else {
                mCurrentNote.setCategoryName(Constants.DEFAULT_CATEGORY);
                mCurrentNote.setCategoryId(getCategoryId(Constants.DEFAULT_CATEGORY));
            }
            mCurrentNote.setDateModified(System.currentTimeMillis());
            noteCloudReference.child(mCurrentNote.getNoteId()).setValue(mCurrentNote);
            makeToast("Note updated");

        } else {
            Note note = new Note();
            note.setNoteType(Constants.NOTE_TYPE_TEXT);
            if (mCurrentCategory != null) {
                note.setCategoryName(mCurrentCategory.getCategoryName());
                note.setCategoryId(mCurrentCategory.getCategoryId());
            }else {
                note.setCategoryName(Constants.DEFAULT_CATEGORY);
                note.setCategoryId(getCategoryId(Constants.DEFAULT_CATEGORY));
            }
            note.setContent(mContent.getText().toString());
            note.setTitle(mTitle.getText().toString());
            String key = noteCloudReference.push().getKey();
            note.setNoteId(key);
            note.setDateCreated(System.currentTimeMillis());
            Calendar calendar2 = GregorianCalendar.getInstance();
            calendar2.add(Calendar.DAY_OF_WEEK, +4);
            calendar2.add(Calendar.MILLISECOND, 10005623);
            note.setDateModified(calendar2.getTimeInMillis());
            noteCloudReference.child(key).setValue(note);
            makeToast("Note added");
        }
        startActivity(new Intent(getActivity(), MainActivity.class));

    }

    private String getCategoryId(String categoryName) {
        for (Category category: mCategories){
            if (!TextUtils.isEmpty(category.getCategoryId()) && category.getCategoryName().equals(categoryName)){
                return category.getCategoryId();
            }
        }
        return addCategoryToFirebase(categoryName);
    }


    private String addCategoryToFirebase(String category) {
        Category cat = new Category();
        cat.setCategoryName(category);
        String key = categoryCloudReference.push().getKey();
        cat.setCategoryId(key);
        categoryCloudReference.child(key).setValue(cat);
        return key;
    }



    private void makeToast(String message){
        Snackbar snackbar = Snackbar.make(mRootView, message, Snackbar.LENGTH_LONG);

        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.primary));
        TextView tv = (TextView)snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snackbar.show();
    }

    public void showChooseCategoryDialog(List<Category> categories) {
        selectCategoryDialog = SelectCategoryDialogFragment.newInstance();
        selectCategoryDialog.setCategories(categories);

        selectCategoryDialog.setCategorySelectedListener(new OnCategorySelectedListener() {
            @Override
            public void onCategorySelected(Category selectedCategory) {
                selectCategoryDialog.dismiss();
                mCategory.setText(selectedCategory.getCategoryName());
                mCurrentCategory = selectedCategory;
            }

            @Override
            public void onEditCategoryButtonClicked(Category selectedCategory) {

            }

            @Override
            public void onDeleteCategoryButtonClicked(Category selectedCategory) {

            }
        });
        selectCategoryDialog.show(getActivity().getFragmentManager(), "Dialog");
    }

    private void promptForDelete(Note note){
        String title = "Delete " + note.getTitle();
        String message =  "Are you sure you want to delete note " + note.getTitle() + "?";


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View titleView = (View)inflater.inflate(R.layout.dialog_title, null);
        TextView titleText = (TextView)titleView.findViewById(R.id.text_view_dialog_title);
        titleText.setText(title);
        alertDialog.show();
        alertDialog.setCustomTitle(titleView);

        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    public void promptForDiscard(){
        String title = "Discard Note";
        String message =  "Are you sure you want to discard note ";


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View titleView = (View)inflater.inflate(R.layout.dialog_title, null);
        TextView titleText = (TextView)titleView.findViewById(R.id.text_view_dialog_title);
        titleText.setText(title);
        alertDialog.setCustomTitle(titleView);

        alertDialog.setMessage(message);
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                resetFields();
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void resetFields() {
        mCategory.setText("");
        mTitle.setText("");
        mContent.setText("");
    }

    public void displayShareIntent() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mTitle.getText().toString());
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mContent.getText().toString());
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));

    }


    private void startPlaying() {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(mLocalAudioFilePath);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }



    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        File recordFile = FileUtils.getattachmentFileName(Constants.MIME_TYPE_AUDIO_EXT);
        mLocalAudioFilePath = recordFile.getAbsolutePath();
        mRecorder.setOutputFile(mLocalAudioFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioEncodingBitRate(96000);
        mRecorder.setAudioSamplingRate(44100);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
            makeToast("Unable to record " + e.getLocalizedMessage());
        }


    }


    private void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }

        String name = "Audio_" + TimeUtils.getDatetimeSuffix(System.currentTimeMillis());
        Attachment attachment = new Attachment(name, mLocalAudioFilePath, Constants.MIME_TYPE_AUDIO);
        mAttachments.add(attachment);
        makeToast("Recording added");


    }


    public void promptToStartRecording(){
        String title = getContext().getString(R.string.start_recording);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View titleView = (View)inflater.inflate(R.layout.dialog_title, null);
        TextView titleText = (TextView)titleView.findViewById(R.id.text_view_dialog_title);
        titleText.setText(title);
        alertDialog.setCustomTitle(titleView);


        alertDialog.setPositiveButton(getString(R.string.start), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startRecording();
                promptToStopRecording();
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

    public void promptToStopRecording(){
        String title = getContext().getString(R.string.stop_recording);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View titleView = (View)inflater.inflate(R.layout.dialog_title, null);
        TextView titleText = (TextView)titleView.findViewById(R.id.text_view_dialog_title);
        titleText.setText(title);
        alertDialog.setCustomTitle(titleView);


        alertDialog.setPositiveButton(getString(R.string.stop), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopRecording();
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }


    //Checks whether the user has granted the app permission to
    //access external storage
    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(LOG_TAG,"Permission is granted");
                return true;
            } else {
                Log.v(LOG_TAG,"Permission is revoked");
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_PERMISSION_REQUEST);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(LOG_TAG,"Permission is granted  API < 23");
            return true;
        }
    }

    //Checks whether the user has granted the app permission to
    //access external storage
    private boolean isRecordPermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (getActivity().checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_REQUEST);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case EXTERNAL_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isRecordPermissionGranted()) {
                        promptToStartRecording();
                    }
                } else {
                    //permission was denied, disable backup
                    makeToast("External Access Denied");
                }
                break;
            case RECORD_AUDIO_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission was granted perform backup
                    promptToStartRecording();
                } else {
                    //permission was denied, disable backup
                    makeToast("Mic Access Denied");
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }





    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_CAPTURE_PERMISSION_REQUEST && resultCode == Activity.RESULT_OK) {
            addPhotoToGallery();
            populateImage(mLocalImagePath, false);
            uploadFileToCloud(mLocalImagePath);
        } else {
            makeToast("Image Capture Failed");

        }
    }

    private void addPhotoToGallery() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mLocalImagePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.getActivity().sendBroadcast(mediaScanIntent);

    }

    private void takePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            makeToast("There was a problem saving the photo...");
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri fileUri = Uri.fromFile(photoFile);
            mImageURI = fileUri;
            mLocalImagePath = fileUri.getPath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageURI);
            startActivityForResult(takePictureIntent, IMAGE_CAPTURE_PERMISSION_REQUEST);
        };
    }


    private void populateImage(String profileImagePath, boolean isCloudImage) {
        mImageAttachment.setVisibility(View.VISIBLE);
        if (isCloudImage) {
            Uri fileToDownload = Uri.fromFile(new File(mLocalImagePath));
            StorageReference imageRef = mImageStorageReference.child(fileToDownload.getLastPathSegment());
            Glide.with(getContext())
                    .using(new FirebaseImageLoader())
                    .load(imageRef)
                    .placeholder(R.drawable.default_image)
                    .centerCrop()
                    .into(mImageAttachment);

        }else {
            Glide.with(getContext())
                    .load(profileImagePath)
                    .placeholder(R.drawable.default_image)
                    .centerCrop()
                    .into(mImageAttachment);

        }
    }


    /**
     * Creates the image file to which the image must be saved.
     * @return
     * @throws IOException
     */
    protected File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = TimeUtils.getDatetimeSuffix(System.currentTimeMillis());
        String imageFileName = "Image_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mLocalImagePath = image.getAbsolutePath();
        return image;
    }


    private void uploadFileToCloud(String filePath){


        Uri fileToUpload = Uri.fromFile(new File(filePath));

        String fileName = fileToUpload.getLastPathSegment();
        Attachment attachment = new Attachment(fileName, mLocalImagePath, Constants.MIME_TYPE_IMAGE);
        mAttachments.add(attachment);

        StorageReference imageRef = mImageStorageReference.child(attachment.getName());

        UploadTask uploadTask = imageRef.putFile(fileToUpload);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                makeToast("Unable to upload image to cloud" + e.getLocalizedMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageUploadedToCloud = true;
                makeToast("Image uploaded successfully");
            }
        });

    }



}
