package com.example.project;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GalleryActivity extends AppCompatActivity implements  StartDragListener,
        ImageDownloader.DownloadCallback{

    private boolean isEditable;
    private RecyclerView galleryRecyclerView;
    private GalleryAdapter adapter;
    private ItemTouchHelper touchHelper;
    private ArrayList<String> gallery;
    private File galleryFolder;
    private ArrayList<String> newImages;
    private String businessID;

    private static final int COLUMNS_COUNT = 4;
    private static final int RC_ADD_IMAGES = 497;

    private static final String TAG = "GalleryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        isEditable = getIntent().getStringExtra("mode").equals("editable");
        gallery = getIntent().getStringArrayListExtra("gallery");
        businessID = getIntent().getStringExtra("businessID");

        galleryRecyclerView = (RecyclerView) findViewById(R.id.galleryRecyclerView);
        galleryFolder = new File(getIntent().getStringExtra("gallery_folder"));
        adapter = new GalleryAdapter(this, gallery, galleryFolder, isEditable, this);
        touchHelper = new ItemTouchHelper(new ImageMoveCallback(adapter));
        galleryRecyclerView.setAdapter(adapter);
        galleryRecyclerView.setLayoutManager(new GridLayoutManager(this, COLUMNS_COUNT));
        touchHelper.attachToRecyclerView(galleryRecyclerView);

        onGalleryUpdate();
        downloadImages();

        findViewById(R.id.deleteBtn).setVisibility(isEditable ? View.VISIBLE : View.GONE);
        findViewById(R.id.addBtn).setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }

    private void onGalleryUpdate() {
        final UploadBroadcastReceiver uploadReceiver = ((AppLoader)getApplicationContext()).getUploadReceiver();
        uploadReceiver.getNewImage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s == null || !businessID.equals(uploadReceiver.getBusiness().getId()) || uploadReceiver.isLogo())
                    return;
                if(!gallery.contains(s)) { // todo: no need for if?
                    gallery.add(s);
                    adapter.notifyItemInserted(gallery.indexOf(s)); // size-1?
                }
                downloadImages();
            }
        });
    }

    private void downloadImages() {
        for(String image : gallery){
            ImageDownloader.getImage(image, businessID, !isEditable, galleryFolder, this);
        }
    }

    public void deleteImagesButton(View view){
        if(adapter.getSelectedImages().size() == 0) {
            showMessage("No images selected to delete.");
            return;
        }

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Delete " + adapter.getSelectedImages().size() + " images?");
        alertDialog.setMessage("Are you sure you want to delete these images?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                gallery.removeAll(adapter.getSelectedImages());
                dialog.dismiss();
                adapter.triggerSelecting();
                adapter.notifyDataSetChanged();
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

    public void addImageButton(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), RC_ADD_IMAGES);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_ADD_IMAGES && resultCode == RESULT_OK) {
            ArrayList<Uri> imageList = new ArrayList<>();
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    imageList.add(data.getClipData().getItemAt(i).getUri());
                }
            } else if (data.getData() != null) {
                Log.d(TAG, data.getData().toString());
                imageList.add(data.getData());
            }
            uploadImages(imageList);
        }
    }

    private void uploadImages(ArrayList<Uri> imageList) {
        WorkManager workManager = WorkManager.getInstance(this);
        Business business = ((AppLoader)getApplicationContext()).getBusiness();

        for(int i=0;i<imageList.size();i++) {
            ImageUploader.addImageUpload(getApplicationContext(), business, imageList.get(i), false);
        }
    }

    private void copyImageToAppFolder(Business business, Uri imageUri, String name) throws IOException {
        File createDir = new File(getFilesDir(), business.getId());
        if(!createDir.exists()) {
            createDir.mkdir();
        }
        File file = new File(galleryFolder, name);
        if(file.exists()){
            return;
        }

        InputStream input = getContentResolver().openInputStream(imageUri);
        Files.copy(input, file.toPath());
        input.close();
    }

    @Override
    public void onImageDownloaded(String businessID, final String imageName) {
        if(!this.businessID.equals(businessID))
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.addDownloadedImage(imageName);
            }
        });
    }
}