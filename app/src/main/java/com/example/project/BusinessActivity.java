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

import com.google.common.io.Files;

import java.io.File;
import java.util.ArrayList;

public class BusinessActivity extends AppCompatActivity {

    private Business business;

    private RecyclerView galleryRecyclerView;
    private GalleryAdapter adapter;
    private ArrayList<String> gallery;
    private File galleryFolder;
    private boolean ownedBusiness;

    private static final int RC_EDIT_BUSINESS = 974;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);
        setupBusiness(getIntent());
        business = ((AppLoader) getApplicationContext()).getBusiness();
        if(business != null)
            downloadImages(0);

        galleryRecyclerView = (RecyclerView) findViewById(R.id.galleryRecyclerView);
        galleryFolder = new File(getIntent().getStringExtra("gallery_folder"));
        adapter = new GalleryAdapter(this, gallery, galleryFolder, false, null);
        galleryRecyclerView.setAdapter(adapter);
        galleryRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
    }

    private void setupBusiness(Intent intent){
        if(intent == null || !intent.hasExtra("business")) {
            business = (Business) getIntent().getParcelableExtra("business");
            ownedBusiness = false;
        }
        else {
            business = ((AppLoader) getApplicationContext()).getBusiness();
            ownedBusiness = true;
        }
        gallery = business.getGallery();
        galleryFolder = new File(getFilesDir(), business.getId());
        if(galleryFolder.exists()) {
//            galleryFolder.createTempDir();
        }
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