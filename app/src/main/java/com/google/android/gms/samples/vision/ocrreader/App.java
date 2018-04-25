package com.google.android.gms.samples.vision.ocrreader;

import android.app.Application;

import com.google.android.gms.samples.vision.ocrreader.manager.AppJobManager;
import com.raizlabs.android.dbflow.config.FlowManager;


public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initApp();
    }

    private void initApp(){
        AppJobManager.getJobManager(this);
//        AppRetrofitManager.getRetrofit();
        FlowManager.init(getApplicationContext());
    }
}
