package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.common.io.Files;

import java.io.File;
import java.util.ArrayList;

public class BusinessActivity extends AppCompatActivity {

    private Business business;

    private RecyclerView galleryRecyclerView;
    private GalleryAdapter adapter;
    private File galleryFolder;
    private boolean ownedBusiness;

    private static final int RC_EDIT_BUSINESS = 974;

    private static final String TAG = "BusinessActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);
        setupBusiness(getIntent());
//        if(business != null)
//            downloadImages(0);

        Log.d(TAG, "in " + business.getId());
        galleryRecyclerView = (RecyclerView) findViewById(R.id.galleryRecyclerView);
        adapter = new GalleryAdapter(this, business.getGallery(), galleryFolder, false, null);
        galleryRecyclerView.setAdapter(adapter);
        galleryRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
    }

    private void setupBusiness(Intent intent){
        if(intent == null || !intent.hasExtra("business")) {
            business = ((AppLoader) getApplicationContext()).getBusiness();
            ownedBusiness = true;
        }
        else {
            business = getIntent().getParcelableExtra("business");
            ownedBusiness = false;
        }

        if(ownedBusiness) {
            galleryFolder = new File(getFilesDir(), business.getId());
            if(!galleryFolder.exists()){
                galleryFolder.mkdir();
            }
        }
        else {
            galleryFolder = Files.createTempDir();
        }
        findViewById(R.id.editBtn).setVisibility(ownedBusiness ? View.VISIBLE : View.GONE);
    }

    public void editBusiness(View view) {
        Intent intent = new Intent(this, EditBusinessActivity.class);
        startActivityForResult(intent, RC_EDIT_BUSINESS);
    }

    private void downloadImages(final int idx) {
        if(idx >= business.getGallery().size())
            return;
        Log.d(TAG, "starting downloading image " + idx);
        FirebaseHandler firebaseHandler = FirebaseHandler.getInstance();
        final LiveData<Boolean> downloadDone = firebaseHandler.getUpdate();
        firebaseHandler.fetchImageForBusiness(business, business.getGallery().get(idx), galleryFolder, !ownedBusiness);
        downloadDone.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(!aBoolean)
                    return;
                Log.d(TAG, "finished downloading image " + idx);
                downloadDone.removeObserver(this);
                downloadImages(idx+1);
            }
        });
    }
}