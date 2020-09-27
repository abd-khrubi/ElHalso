package com.example.project;

import android.app.Application;
import android.content.IntentFilter;

// created to easily retrieve current user/business throughout the application
public class AppLoader extends Application {

    private User user;
    private Business business;
    private UploadBroadcastReceiver uploadReceiver;

    public static final String UPLOAD_BROADCAST = "business_updated";

    @Override
    public void onCreate() {
        super.onCreate();
        uploadReceiver = new UploadBroadcastReceiver();
        registerReceiver(uploadReceiver, new IntentFilter(UPLOAD_BROADCAST));
    }

    public User getUser() {
        return user;
    }

    public Business getBusiness() {
        return business;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public UploadBroadcastReceiver getUploadReceiver() {
        return uploadReceiver;
    }
}
