package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class BusinessActivity extends AppCompatActivity {

    private Business business;
    private static final int RC_EDIT_BUSINESS = 974;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);
        business = ((AppLoader) getApplicationContext()).getBusiness();
        if(business != null)
            downloadImages(0);
    }

    public void editBusiness(View view) {
        Intent intent = new Intent(this, EditBusinessActivity.class);
        startActivityForResult(intent, RC_EDIT_BUSINESS);
    }

    private void downloadImages(final int idx) {
        if(idx >= business.getGallery().size())
            return;
        Log.d("BusinessActivity", "starting downloading image " + idx);
        FirebaseHandler firebaseHandler = FirebaseHandler.getInstance();
        final LiveData<Boolean> downloadDone = firebaseHandler.getUpdate();
        firebaseHandler.fetchImageForBusiness(business, business.getGallery().get(idx), getFilesDir());
        downloadDone.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(!aBoolean)
                    return;
                Log.d("BusinessActivity", "finished downloading image " + idx);
                downloadDone.removeObserver(this);
                downloadImages(idx+1);
            }
        });
    }
}