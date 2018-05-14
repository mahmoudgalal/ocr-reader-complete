package com.google.android.gms.samples.vision.ocrreader.ui;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;

import com.birbit.android.jobqueue.JobManager;
import com.google.android.gms.samples.vision.ocrreader.R;
import com.google.android.gms.samples.vision.ocrreader.databinding.ActivityMainBinding;
import com.google.android.gms.samples.vision.ocrreader.events.EventHandler;
import com.google.android.gms.samples.vision.ocrreader.jobs.ImageJobs;
import com.google.android.gms.samples.vision.ocrreader.manager.AppJobManager;
import com.google.android.gms.samples.vision.ocrreader.ocr.OcrDetectorProcessor;
import com.google.android.gms.samples.vision.ocrreader.ui.customcamera.CameraActivity;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import br.com.simplepass.loading_button_lib.Utils;

public class MainActivity extends AppCompatActivity
        implements MainActivity.OnAlignedTextPrepared {

    public OcrDetectorProcessor.OnAlignedTextPrepared onAlignedTextPreparedListener;
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 2;
    public static final int REQUEST_READ_EXTERNAL_STORAGE = 3;


    private static final String TAG = MainActivity.class.getName();
    String mCurrentPhotoPath;

    private Timer mTimer1;
    private TimerTask mTt1;
    private Handler mTimerHandler = new Handler();

    Intent intent;
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

                Context context = getApplicationContext();
                TextRecognizer ocrFrame = new TextRecognizer.Builder(context).build();
                Frame frame = new Frame.Builder().setBitmap(photo).build();
                if (ocrFrame.isOperational()) {
                    Log.e(TAG, "Textrecognizer is operational");
                }
                SparseArray<TextBlock> textBlocks = ocrFrame.detect(frame);
                List<TextBlock> textBlockList = new ArrayList<>();
                for (int i = 0; i < textBlocks.size(); i++) {
                    textBlockList.add(textBlocks.get(i));
                }

                for (int i = 0; i < textBlocks.size(); i++) {
                    TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));

//                    Log.d(TAG, "onSync: "+ textBlock.getValue());
//                    Log.e(TAG, "something is happening");
                    System.out.println(i + "                        " + textBlock.getValue());
                }
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

    @Override
    public void onAlignedTextPrepared(Map<Long, List<String>> allBlocksAligned) {

    }


    class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0].getApplicationContext();
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            final String packageName = context.getPackageName();
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }


    private void logAlignedBlocks(List<TextBlock> items) {
        Log.d(TAG, "logAlignedBlocks: *************************************");
        if (items.isEmpty())
            return;
        Map<Long, List<String>> allBlocksAligned = new HashMap<>();
        long id = 0;
        List<TextBlock> sortedBlocks = items;
        Collections.sort(sortedBlocks, new Comparator<TextBlock>() {
            @Override
            public int compare(TextBlock textBlock, TextBlock t1) {
                return textBlock.getBoundingBox().left - t1.getBoundingBox().left;
            }
        });

        for (int i = 0; i < sortedBlocks.size(); ++i) {
            TextBlock item = sortedBlocks.get(i);
            if (item == null)
                continue;

            for (int j = i + 1; j < sortedBlocks.size(); ++j) {
                TextBlock innerItem = sortedBlocks.get(j);
                if (Math.abs(item.getBoundingBox().top - innerItem.getBoundingBox().top) < 8) {
                    //we found almost aligned items
                    id = item.getBoundingBox().top;//Identify by Y coordinate
                    List<String> alignedItems = allBlocksAligned.get(id);
                    if (alignedItems == null) {
                        alignedItems = new ArrayList<>();
                        allBlocksAligned.put(id, alignedItems);
                        alignedItems.add(item.getValue());
                    }
                    if (!alignedItems.contains(innerItem.getValue()))
                        alignedItems.add(innerItem.getValue());
                    for (String s : alignedItems) {
                        System.out.print(s + " ");
                    }
                }
            }

        }
        if (onAlignedTextPreparedListener != null)
            onAlignedTextPreparedListener.onAlignedTextPrepared(allBlocksAligned);

    }

    public interface OnAlignedTextPrepared {
        // Passes you a map where each item containing a list of all items on the same horizontal line.
        void onAlignedTextPrepared(Map<Long, List<String>> allBlocksAligned);
    }
}

