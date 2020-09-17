package com.example.project;

import android.app.Application;

// created to easily retrieve current user/business throughout the application
public class AppLoader extends Application {

    private User user;
    private Business business;

    @Override
    public void onCreate() {
        super.onCreate();
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
}
