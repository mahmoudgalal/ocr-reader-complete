package com.google.android.gms.samples.vision.ocrreader.manager;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
//import com.google.android.gms.samples.vision.ocrreader.Services.AppGcmJobService;
import com.google.android.gms.samples.vision.ocrreader.Services.AppJobService;

public class AppJobManager {

    private static JobManager mJobManager;

    public static synchronized JobManager getJobManager() {
        return mJobManager;
    }

    public static synchronized JobManager getJobManager(Context context) {
        if (mJobManager == null) {
            configureJobManager(context);
        }
        return mJobManager;
    }


    private static synchronized void configureJobManager(Context context) {
        if (mJobManager == null) {
            Configuration.Builder builder = new Configuration.Builder(context).
                    minConsumerCount(1).
                    maxConsumerCount(10).
                    loadFactor(3).
                    consumerKeepAlive(120).
                    customLogger(new CustomLogger() {
                        private static final String TAG = "Jobs";

                        @Override
                        public boolean isDebugEnabled() {
                            return true;
                        }

                        @Override
                        public void d(String text, Object... args) {
                            Log.d(TAG, String.format(text, args));
                        }

                        @Override
                        public void e(Throwable t, String text, Object... args) {
                            Log.d(TAG, String.format(text, args));
                        }

                        @Override
                        public void e(String text, Object... args) {
                            Log.d(TAG, String.format(text, args));
                        }

                        @Override
                        public void v(String text, Object... args) {
                        }
                    });

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                builder.scheduler(
                        FrameworkJobSchedulerService.createSchedulerFor(context, AppJobService.class),
                        true
                );
            }else{
                int enableGCM = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
                if(enableGCM == ConnectionResult.SUCCESS) {
//                    builder.scheduler(
////                            GcmJobSchedulerService.createSchedulerFor(context, AppGcmJobService.class),
//                            true
//                    );
                }
            }
            mJobManager = new JobManager(builder.build());
        }

    }

}
