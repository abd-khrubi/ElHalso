package com.example.project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity implements  StartDragListener{

    private boolean isEditable;
    private RecyclerView galleryRecyclerView;
    private GalleryAdapter adapter;
    private ItemTouchHelper touchHelper;
    private ArrayList<String> gallery;
    private ArrayList<String> newImages;

    private static final int COLUMNS_COUNT = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        isEditable = getIntent().getStringExtra("mode").equals("editable");
        gallery = getIntent().getStringArrayListExtra("gallery");

        galleryRecyclerView = (RecyclerView) findViewById(R.id.galleryRecyclerView);
        adapter = new GalleryAdapter(gallery, isEditable, this);
        touchHelper = new ItemTouchHelper(new ImageMoveCallback(adapter));
        galleryRecyclerView.setAdapter(adapter);
        galleryRecyclerView.setLayoutManager(new GridLayoutManager(this, COLUMNS_COUNT));
        touchHelper.attachToRecyclerView(galleryRecyclerView);

        findViewById(R.id.deleteBtn).setVisibility(isEditable ? View.VISIBLE : View.GONE);
        findViewById(R.id.addBtn).setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }

    public void deleteImagesButton(View view){
        if(adapter.getSelectedImages().size() == 0) {
            showMessage("No images selected to delete.");
            return;
        }

        // todo: show dialog?
        gallery.removeAll(adapter.getSelectedImages());
        adapter.notifyDataSetChanged();
    }

    public void addImageButton(View view) {
        // todo: update newImages arraylist
    }

    public void okButton(View view) {
        Intent backIntent = new Intent();
        backIntent.putStringArrayListExtra("gallery", gallery);
        setResult(RESULT_OK, backIntent);
        finish();
    }

    private void showMessage(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if(adapter.getIsSelecting()) {
            adapter.triggerSelecting();
        }
        else {
            Intent backIntent = new Intent();
            backIntent.putStringArrayListExtra("gallery", gallery);
            setResult(RESULT_OK, backIntent);
            finish();
        }
    }

    @Override
    public void requestDrag(ImageHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }
}