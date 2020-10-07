package com.example.project;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;

import com.example.project.data.Business;
import com.example.project.data.User;
import com.example.project.location.LocationInfo;
import com.example.project.location.LocationTracker;
import com.google.firebase.auth.FirebaseAuth;


public class AppLoader extends Application {

    private static final String TAG = "AppLoader";

    private User user;
    private Business business;
    private UploadBroadcastReceiver uploadReceiver;
    private LocationTracker locationTracker;
    private LocationInfo locationInfo;
    private AlertDialog loadingDialog;

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

    public LocationTracker getLocationTracker() {
        return locationTracker;
    }

    public void logout(final Context context) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
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

    public void setRadius(int radius) {
        this.user.setRadius(radius);
    }

    public void showLoadingDialog(Context context, String title, String message) {
        if (loadingDialog != null) {
            loadingDialog.setMessage(title);
            loadingDialog.setMessage(message);
            return;
        }
        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);
        loadingDialog = new AlertDialog.Builder(context).create();
        loadingDialog.setTitle(title);
        loadingDialog.setMessage(message);
        loadingDialog.setView(progressBar);
        loadingDialog.setCancelable(false);
        loadingDialog.show();
    }

    public void dismissLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }
}
