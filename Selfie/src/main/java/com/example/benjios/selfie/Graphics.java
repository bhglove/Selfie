package com.example.benjios.selfie;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 *Loads the temporary file from the first activity, rotate it 90 degrees, and then
 * displays it to the user. There are two buttons. One that displays a toast when text is entered
 * and one that saves the file to the devices
 *
 * Sources:
 *
 * This source demonstrated how to retrieve an imageview from a file
 * http://stackoverflow.com/questions/4181774/show-image-view-from-file-path-in-android
 *
 * This source demonstrated how to rotate a bitmap 90 degrees
 * http://stackoverflow.com/questions/9015372/how-to-rotate-a-bitmap-90-degrees
 *
 * This source demonstrated how to set a bitmap to imageview.
 * http://stackoverflow.com/questions/8306623/get-bitmap-attached-to-imageview
 */

public class Graphics extends AppCompatActivity {
    private EditText mImageText;
    private ImageView mImage;
    private String filePath = null;

    /**
     * Saves the picture to the users device.
     * @return void
     */
    private void savePicture(){
        String fileName = "";
        EditText mFileName = (EditText) findViewById(R.id.saveText);

        if(!mFileName.getText().toString().isEmpty()){
            fileName = mFileName.getText().toString();
        }

        // Save a file: path for use with ACTION_VIEW intents
        Bitmap image = ((BitmapDrawable) mImage.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();

        File file = new File(filePath);

        //Checks if TextBox was given a name
        if(fileName.isEmpty()){
            fileName = file.getName();
        }
        else{
            String parent = file.getParent();
            //if(file.exists()) file.delete();
            File temp = new File(parent, fileName + ".jpeg");
            file = temp;
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            out.write(data);
            out.flush();
            out.close();

            file.setReadable(true);

            /* Code from stack overflow that adds metadata to photo and allow it to be
                  automatically viewed in the gallery.
            */
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, file.getName());
            values.put(MediaStore.Images.Media.DESCRIPTION, "Really cool selfie!");
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.ImageColumns.BUCKET_ID, image.toString().toLowerCase
                    (Locale.US).hashCode());
            values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    file.getName().toLowerCase(Locale.US));
            values.put("_data", file.getAbsolutePath());

            ContentResolver cr = getContentResolver();
            cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        }catch (IOException e) {
        e.printStackTrace();
        }
    }

    /**
     *Returns a boolean based on if the TextView mImageText contains phrase.
     * @return boolean
     */
    private  boolean setmImageText(){
        boolean result = false;

        if (!mImageText.getText().toString().isEmpty()) {
            result = true;
        }

        return result;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graphics);
        mImage = (ImageView) findViewById(R.id.imageView);
        mImage.setVisibility(View.VISIBLE);
        mImageText = (EditText) findViewById(R.id.imageText);
        filePath = getIntent().getStringExtra("FILE");

        File mPic = new File(filePath);
        /**
          Rotates the bitmap 270 degrees to fix saving error.
         */
        if(mPic.exists()){
            Bitmap image = BitmapFactory.decodeFile(mPic.getAbsolutePath());
            Matrix matrix = new Matrix();

            matrix.postRotate(270);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, image.getWidth(),
                    image.getHeight(), true);

            Bitmap rotatedBitmap = Bitmap.createBitmap
            (scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

            mImage.setImageBitmap(rotatedBitmap);
        }
        mPic.deleteOnExit();

        Button mSaveButton = (Button) findViewById(R.id.saveButton);
        Button mApplyButton = (Button) findViewById(R.id.imageTextButton);

        mApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(setmImageText()){
                    Toast.makeText(getApplicationContext(),"Cool phrase!",
                            Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(getApplicationContext(),"Enter a cool phrase!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mImageText.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Enter a cool phrase!",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getApplicationContext(), "Saving...", Toast.LENGTH_SHORT).show();
                savePicture();
                Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Graphics.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
