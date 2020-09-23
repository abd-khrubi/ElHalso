package com.example.project;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.FileHandler;

public class GalleryActivity extends AppCompatActivity implements  StartDragListener {

    private boolean isEditable;
    private RecyclerView galleryRecyclerView;
    private GalleryAdapter adapter;
    private ItemTouchHelper touchHelper;
    private ArrayList<String> gallery;
    private File galleryFolder;
    private ArrayList<String> newImages;

    private static final int COLUMNS_COUNT = 4;
    private static final int RC_ADD_IMAGES = 497;

    private static final String TAG = "GalleryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        isEditable = getIntent().getStringExtra("mode").equals("editable");
        gallery = getIntent().getStringArrayListExtra("gallery");

        galleryRecyclerView = (RecyclerView) findViewById(R.id.galleryRecyclerView);
        galleryFolder = new File(getIntent().getStringExtra("gallery_folder"));
        adapter = new GalleryAdapter(this, gallery, galleryFolder, isEditable, this);
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
        // todo: update newImages arraylist

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
        Business business = ((AppLoader)getApplicationContext()).getBusiness();

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
            uploadImages(business, imageList, 0);
        }
    }

    // todo: make it on another thread?
    public void uploadImages(final Business business, final ArrayList<Uri> imageList, final int idx){
        if(idx >= imageList.size())
            return;

        final String imageName = DocumentFile.fromSingleUri(this, imageList.get(idx)).getName();
        final FirebaseHandler firebaseHandler = FirebaseHandler.getInstance();
        final LiveData<Boolean> updateDone = firebaseHandler.getUpdate();
        firebaseHandler.addImageToBusinessGallery(business, imageList.get(idx), imageName);
        updateDone.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(!aBoolean)
                    return;
                updateDone.removeObserver(this);
                //todo: copy to app folder
                String imageName = (String) FirebaseHandler.getInstance().getUpdatedObject();
                try {
                    copyImageToAppFolder(business, imageList.get(idx), imageName);
                    gallery.add(imageName);
                    adapter.notifyDataSetChanged();
                } catch (Exception e){
                    Log.e(TAG, e.toString());
                }
                Log.d(TAG, "image <" + imageName + "> uploaded");
                uploadImages(business, imageList, idx+1);
            }
        });
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

//    @Override
//    public void drawImage(ImageHolder holder, File galleryFolder, String image) {
//        Bitmap bitmap = null;
//        File imageFile = new File(galleryFolder, image);
//        if(!imageFile.exists()) {
//            Log.d(TAG, "file doesnt exists.");
//            return;
//        }
//
//        try {
//            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(imageFile));
//            int width = (int) getResources().getDimension(R.dimen.gallery_image_width);
//            int height = (int) getResources().getDimension(R.dimen.gallery_image_height);
//            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
//            holder.imageView.setImageBitmap(bitmap);
//        } catch (Exception e) {
//            Log.d(TAG, e.toString());
//        }
//    }
}