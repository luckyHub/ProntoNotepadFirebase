package com.okason.prontonotepad.util;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by Valentine on 1/21/2017.
 */

public class FileUtils {
    public static File getattachmentFileName(String format) {
        File folder = new File(Environment.getExternalStoragePublicDirectory(Constants.ATTACHMENTS_FOLDER), "");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File recordFile = new File(folder, TimeUtils.getDatetimeSuffix(System.currentTimeMillis()) + format);
        return recordFile;

    }

    /**
     * Creates the image file to which the image must be saved.
     * @return
     * @throws IOException
     */
    public static File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = TimeUtils.getDatetimeSuffix(System.currentTimeMillis());
        String imageFileName = "Image_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                Constants.MIME_TYPE_IMAGE_EXT,         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }



}
