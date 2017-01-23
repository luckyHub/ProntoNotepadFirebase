package com.okason.prontonotepad.ui.sketch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.okason.prontonotepad.R;
import com.okason.prontonotepad.util.Constants;
import com.okason.prontonotepad.util.TimeUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SketchActivity extends Activity {

    private final static String LOG_TAG = SketchActivity.class.getSimpleName();
    private ImageButton saveButton, deleteButton, cancelButton;
    private CustomView customView;
    private File sketchDirectory;
    private File sketchFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sketch);

        saveButton = (ImageButton)findViewById(R.id.button_save);
        deleteButton = (ImageButton) findViewById(R.id.button_delete_all);
        cancelButton = (ImageButton) findViewById(R.id.button_cancel);
        customView = (CustomView) findViewById(R.id.custom_view);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveThisDrawing();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customView.clear();
            }
        });

    }


    public void saveThisDrawing()
    {
        sketchDirectory = new File(Environment.getExternalStorageDirectory(), Constants.ATTACHMENTS_FOLDER);
        if (!sketchDirectory.exists()) {
            sketchDirectory.mkdir();
        }




        customView.setDrawingCacheEnabled(true);


        try {
            String imTitle = "Sketch" + "_" + TimeUtils.getDatetimeSuffix(System.currentTimeMillis())+ Constants.MIME_TYPE_SKETCH_EXT;
            sketchFile = new File(sketchDirectory, imTitle);
            if (!sketchFile.exists()){
                sketchFile.createNewFile();
            }
            sketchFile.setReadable(true);

            customView.setDrawingCacheEnabled(true);
            FileOutputStream fOut = new FileOutputStream(sketchFile);
            Bitmap bm =  customView.getDrawingCache();
            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);


        } catch (FileNotFoundException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();

        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
            finish();
        }

        String signatureFilePath = sketchFile.getPath();
        Intent data = new Intent();
        data.setData(Uri.parse(signatureFilePath));
        setResult(RESULT_OK, data);
        finish();
        customView.destroyDrawingCache();
    }




}
