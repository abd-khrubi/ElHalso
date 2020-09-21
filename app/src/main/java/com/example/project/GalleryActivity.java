package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

public class GalleryActivity extends AppCompatActivity implements  StartDragListener{

    private boolean isEditable;
    private Business business;
    private RecyclerView galleryRecyclerView;
    private GalleryAdapter adapter;
    private ItemTouchHelper touchHelper;

    private static final int COLUMNS_COUNT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        isEditable = getIntent().getStringExtra("mode").equals("editable");
        business = ((AppLoader) getApplicationContext()).getBusiness();

        galleryRecyclerView = (RecyclerView) findViewById(R.id.galleryRecyclerView);
        adapter = new GalleryAdapter(business, isEditable, this);
        touchHelper = new ItemTouchHelper(new ImageMoveCallback(adapter));
        galleryRecyclerView.setAdapter(adapter);
        galleryRecyclerView.setLayoutManager(new GridLayoutManager(this, COLUMNS_COUNT));
        touchHelper.attachToRecyclerView(galleryRecyclerView);
    }

    @Override
    public void onBackPressed() {
        if(adapter.getIsSelecting()) {
            adapter.triggerSelecting();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void requestDrag(ImageHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }
}