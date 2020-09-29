package com.example.project;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.appcompat.app.AlertDialog;

import com.google.firebase.auth.FirebaseAuth;


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

    public void logout(final Context context){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Logout");
        alertDialog.setMessage("Are you sure you wish to logout?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                FirebaseAuth.getInstance().signOut();
                setUser(null);
                setBusiness(null);
                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
