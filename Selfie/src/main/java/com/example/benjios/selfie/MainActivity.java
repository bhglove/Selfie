package com.example.benjios.selfie;


import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.jar.Manifest;

/**
 *Benjamin Glover
 * January 24, 2016
 *
 * This activity emulates the front facing camera and displays it to the user. A button is created
 * to promt the user to take a selfie.
 *
 * Sources:
 *
 * Used to create the camera preview in the surfaceview variable.
 *     http://www.java2s.com/Code/Android/Hardware/Camerapreview.htm
 *
 * Used to get the optimal resolution for camera preview.
 *     http://stackoverflow.com/questions/19577299/android-camera-preview-stretched
 *
 */
public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera.ShutterCallback, Camera.PictureCallback{
    private Camera mCamera;
    private SurfaceView mPreview;
    private ImageButton mCameraButton;
    private ImageButton mNextButton;
    private String mCurrentPhotoPath;
    private File mData = null;
    private final int MY_PERMISSIONS_REQUEST_CAMERA = 0;
    private final int MY_PERMISSIONS_REQUEST_WRITE = 1;
    private int permissionCheck = -1;


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;
        mData = null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
    public void showAbout(View v){
        Intent intent = new Intent(getApplicationContext(), InfoActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.CAMERA)) {

            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }
        }
        permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE);
            }
        }
        if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            mNextButton = (ImageButton) findViewById(R.id.nextButton);
            mNextButton.setVisibility(View.INVISIBLE);

            mPreview = (SurfaceView) findViewById(R.id.surfaceView);
            mPreview.getHolder().addCallback(this);


               mPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
               if (Camera.getNumberOfCameras() >= 2) {
                   mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
               } else {
                   mCamera = Camera.open();
               }
               mCameraButton = (ImageButton) findViewById(R.id.cameraButton);
               mCameraButton.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       //mCamera.setPreviewCallback(null);
                       mCamera.takePicture(MainActivity.this, null, MainActivity.this);
                   }
               });
               mNextButton.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       Intent intent = new Intent(getApplicationContext(), Graphics.class);
                       intent.putExtra("FILE", mData.getAbsolutePath().toString());
                       startActivity(intent);
                   }
               });
           }
        else{
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_start);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            case MY_PERMISSIONS_REQUEST_WRITE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            }
            // permissions this app might request
        }

    @Override
    protected void onPause() {
        super.onPause();
        if(permissionCheck != -1) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mCameraButton.setVisibility(View.VISIBLE);
            mNextButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


        @Override
        public void onDestroy () {
            super.onDestroy();
        }

        public void onCancelClick (View v){
            finish();
        }

        public void onSnapClick (View v){
            mCamera.takePicture(this, null, null, this);
        }

        @Override
        public void onShutter () {
            Toast.makeText(this, "Click!", Toast.LENGTH_SHORT).show();
        }

        //TODO onPictureTaken is not being called, thats why intent bundles are not working.
        @Override
        public void onPictureTaken(byte[] data, Camera camera){
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_" + ".jpeg";
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);

            mCameraButton.setVisibility(View.INVISIBLE);
            try {

                File directory = new File(storageDir + "/Selfie");
                if(directory.mkdirs())
                    Log.d("FILE", "Directory made");

                File image = new File(directory, imageFileName);
                FileOutputStream out = new FileOutputStream(image);
                out.write(data);
                out.flush();
                out.close();

                mData = image;
            } catch (IOException e) {
                e.printStackTrace();
            }
            mNextButton.setVisibility(View.VISIBLE);
        }

        @Override
        public void surfaceChanged (SurfaceHolder holder,int format, int width, int height){
            Camera.Parameters params = mCamera.getParameters();
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();

            /*Modified sample code to get optimal camera preview sized based on the dimesions of the
              surface preview.
            */
            Camera.Size optimal = getOptimalPreviewSize
                    (sizes, mPreview.getWidth(), mPreview.getHeight());

            params.setPreviewSize(optimal.width, optimal.height);
            mCamera.setParameters(params);

            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
        }

        @Override
        public void surfaceCreated (SurfaceHolder holder){
            if(mCamera == null){
                if(Camera.getNumberOfCameras() >= 2) {
                    mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                }

                else{
                    mCamera = Camera.open();
                }
            }

            try {
                mCamera.setPreviewDisplay(mPreview.getHolder());
            } catch (IOException e) {
                Log.d("Camera", "Problem opening camera");
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed (SurfaceHolder holder){

        }
}
