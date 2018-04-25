package com.google.android.gms.samples.vision.ocrreader.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.google.android.gms.samples.vision.ocrreader.data.ReceiptRepository;
import com.google.android.gms.samples.vision.ocrreader.exception.ErrorRequestException;
import com.google.android.gms.samples.vision.ocrreader.model.Receipt;
import com.google.android.gms.samples.vision.ocrreader.utils.Constants;


public class ImageJobs extends Job {

    public static final String TAG = "ImageOPeration";
    private String imageUrl = null;
    private String uuid = null;

    public ImageJobs(String imageUrl, String uuid) {
        super(
                new Params(Constants.PRIORITY_NORMAL).
                        requireNetwork().
                        singleInstanceBy(TAG).
                        addTags(TAG)
        );
        this.imageUrl = imageUrl;
        this.uuid = uuid;
    }

    @Override
    public void onAdded() {
        // TODO: 4/18/2018 store in database
        Log.d(TAG , "added");
    }

    @Override
    public void onRun() throws Throwable {
        // TODO: 4/18/2018 getimageUrl from dataBase
        // TODO: 4/18/2018 making call to upload it
        Log.d(TAG , "onRun");
        /*
        ApiService mApiService = AppRetrofitManager.getApiService();

        // this part to create image file from imageUrl
        File imageFile = new File(imageUrl);
        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("image", imageFile.getName(), requestBody);

        // make calling of upload
        Call<String> request = mApiService.uploadImage(fileToUpload);
        String result = AppRetrofitManager.performRequest(request); // it should return serverId to me 
        */
        // TODO: 4/18/2018 add serverId to database
        Receipt receipt = new Receipt();
        receipt.setImageUrl("");
        receipt.setUUID("");
        ReceiptRepository.getInstance().saveReceipt(receipt);

        // TODO: 4/18/2018 use eventBus here to show indication or notification


    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        // TODO: 4/18/2018 use eventBus here to show indication or notification
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        if (throwable instanceof ErrorRequestException) {
            ErrorRequestException error = (ErrorRequestException) throwable;
            int statusCode = error.getResponse().raw().code();
            if (statusCode >= 400 && statusCode < 500) {
                return RetryConstraint.CANCEL;
            }
        }
        return RetryConstraint.RETRY;
    }
}
