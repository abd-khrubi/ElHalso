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

public class GalleryActivity extends AppCompatActivity implements  StartDragListener,ImageDrawer {

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
//                Bitmap bitmap1 = null, bitmap2 = null;

//                bitmap2 = Bitmap.createScaledBitmap(bitmap2,150, 150, tru//                try {
////                    bitmap1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageList.get(0));
////                    bitmap2 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageList.get(1));
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////                bitmap1 = Bitmap.createScaledBitmap(bitmap1,150, 150, true);
////                ((ImageButton)findViewById(R.id.tempImg)).setImageBitmap(bitmap1);e);
//                ((ImageButton)findViewById(R.id.tempImg2)).setImageBitmap(bitmap2);

//            else if(data.getData() != null){
//                Uri uri = data.getData();
////                FirebaseHandler.getInstance().addImageToBusinessGallery(new Business(), uri, "image2.jpg");
//                Log.d(TAG, uri.getLastPathSegment());
//                Log.d(TAG, uri.getPath());
//                Log.d(TAG, DocumentFile.fromSingleUri(this, uri).getName());
////                Bitmap bitmap= BitmapFactory.decodeFile(uri.getPath());
//                Bitmap bitmap = null;
//                try {
//                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                bitmap = Bitmap.createScaledBitmap(bitmap,((ImageButton)findViewById(R.id.tempImg)).getWidth(), ((ImageButton)findViewById(R.id.tempImg)).getHeight(), true);
//                ((ImageButton)findViewById(R.id.tempImg)).setImageBitmap(bitmap);
////                ((ImageButton)findViewById(R.id.tempImg)).setImageURI(uri);
//            }
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
                } catch (FileNotFoundException e) {
                    Log.e(TAG, e.toString());
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
//        if(file.exists()){
//            return;
//        }
//        OutputStream out;
//        try {
//            file.createNewFile();
//            out = new FileOutputStream(file);
//        } catch (Exception e){
//            Log.e(TAG, e.toString());
//            return;
//        }
//        out.write(data);
//        out.close();

        InputStream input = getContentResolver().openInputStream(imageUri);
        OutputStream output = new FileOutputStream(file);
        Files.copy(input, file.toPath());

//        FileUtils.copy(input, output);
    }

//    private String getRealPathFromURI(Uri contentUri) {
//
//        String[] proj = { MediaStore.Video.Media.DATA };
//        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
//        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//        cursor.moveToFirst();
//        return cursor.getString(column_index);
//    }

//    private String getRealPathFromURI(Uri contentURI) {
//        String result;
//        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
//        if (cursor == null) {
//            result = contentURI.getPath();
//        } else {
//            cursor.moveToFirst();
//            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
//            result = cursor.getString(idx);
//            cursor.close();
//        }
//        return result;
//    }

//    public String getRealPathFromURI(Uri contentUri)
//    {
//        String[] proj = { MediaStore.Audio.Media.DATA };
//        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
//        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
//        cursor.moveToFirst();
//        return cursor.getString(column_index);
//    }

    public String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public void drawImage(ImageHolder holder, File galleryFolder, String image) {
        Bitmap bitmap = null;
        File imageFile = new File(galleryFolder, image);
        if(!imageFile.exists()) {
            Log.d(TAG, "file doesnt exists.");
            return;
        }

        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(imageFile));
            bitmap = Bitmap.createScaledBitmap(bitmap,holder.imageView.getWidth(), holder.imageView.getHeight(), true);
            holder.imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }
}