package com.example.project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class UploadBroadcastReceiver extends BroadcastReceiver {

    private final MutableLiveData<String> newImage  = new MutableLiveData<>();
    private Business business;
    private boolean isLogo;

    private static final String TAG = "UploadReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!intent.getAction().equals(AppLoader.UPLOAD_BROADCAST))
            return;

        business = intent.getParcelableExtra("business");
        isLogo = intent.getBooleanExtra("isLogo", false);
        newImage.postValue(intent.getStringExtra("newImage"));
    }

    public LiveData<String> getNewImage(){
        return newImage;
    }

    public Business getBusiness() {
        return business;
    }

    public boolean isLogo() {
        return isLogo;
    }
}