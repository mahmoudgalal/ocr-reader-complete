package com.google.android.gms.samples.vision.ocrreader.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.birbit.android.jobqueue.JobManager;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.databinding.ActivityMainBinding;
import com.google.android.gms.samples.vision.ocrreader.events.EventHandler;
import com.google.android.gms.samples.vision.ocrreader.jobs.ImageJobs;
import com.google.android.gms.samples.vision.ocrreader.manager.AppJobManager;
import com.google.android.gms.samples.vision.ocrreader.ocr.OcrFromImage;
import com.google.android.gms.samples.vision.ocrreader.ui.customcamera.CameraActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    public static final int REQUEST_READ_EXTERNAL_STORAGE = 3;


    private static final String TAG = MainActivity.class.getName();
    String mCurrentPhotoPath;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setHandler(new EventHandler() {
            @Override
            public void onAddReceipt() {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            }

            @Override
            public void onSync() {
                Log.d("sync", "Clicked");
//                binding.btnId.startAnimation();
//                binding.btnId.stopAnimation();
                Bitmap photo = BitmapFactory.decodeResource(getResources(), R.drawable.simple);
                System.out.println(OcrFromImage.getInstance().getTextFromImage(MainActivity.this,photo));

            }
        });
    }

    /**
     *
     */
    public void enableRuntimePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.CAMERA)) {

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            dispatchTakePictureIntent();
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            }
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {
        switch (RC) {
            case REQUEST_IMAGE_CAPTURE:
            case REQUEST_READ_EXTERNAL_STORAGE:
            case REQUEST_WRITE_EXTERNAL_STORAGE:
                Log.d("request permission", " done");
                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                    Toast.makeText(MainActivity.this, "Permission Granted, Now your application can access CAMERA.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission Canceled, Now your application cannot access CAMERA.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            String serialId = UUID.randomUUID().toString();
            JobManager ref = AppJobManager.getJobManager();
            ref.addJobInBackground(new ImageJobs("", serialId));
            Log.d("ActivityResult", "done");
        }
    }

    /**
     * taking picture code from google developer
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
            // TODO: 4/19/2018 catch exception and show toast for example
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = Uri.fromFile(photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("Image Path :", mCurrentPhotoPath);
        return image;
    }

}
