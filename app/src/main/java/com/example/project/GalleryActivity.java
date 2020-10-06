package com.example.project;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.adapters.GalleryAdapter;
import com.example.project.data.Business;

import java.io.File;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity implements  StartDragListener,
        ImageDownloader.DownloadCallback{

    private boolean isEditable;
    private RecyclerView galleryRecyclerView;
    private GalleryAdapter adapter;
    private ItemTouchHelper touchHelper;
    private File galleryFolder;
    private Business business;
    private Menu menu;

    private static final int COLUMNS_COUNT = 4;
    private static final int RC_ADD_IMAGES = 497;

    private static final String TAG = "GalleryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        if(getIntent().hasExtra("business")) {
            business = getIntent().getParcelableExtra("business");
            galleryFolder = new File(getIntent().getStringExtra("galleryFolder"));
            isEditable = false;
        }
        else {
            business = ((AppLoader)getApplicationContext()).getBusiness();
            galleryFolder = new File(getFilesDir(), business.getId());
            isEditable = true;
        }
        findViewById(R.id.noGalleryTxt).setVisibility(business.getGallery().size() > 0 ? View.GONE : View.VISIBLE);

        galleryRecyclerView = (RecyclerView) findViewById(R.id.galleryRecyclerView);
        adapter = new GalleryAdapter(this, business.getGallery(), galleryFolder, isEditable, this);
        touchHelper = new ItemTouchHelper(new ImageMoveCallback(adapter));
        galleryRecyclerView.setAdapter(adapter);
        galleryRecyclerView.setLayoutManager(new GridLayoutManager(this, COLUMNS_COUNT));
        touchHelper.attachToRecyclerView(galleryRecyclerView);

        onGalleryUpdate();
        downloadImages();

        adapter.getSelectedImagesSize().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer size) {
                if(size == 0){
                    getSupportActionBar().setTitle(business.getName());
                    menu.findItem(R.id.action_delete).setVisible(false);
                    return;
                }
                getSupportActionBar().setTitle(size + " Images Selected");
                menu.findItem(R.id.action_delete).setVisible(true);
            }
        });

        setSupportActionBar((Toolbar) findViewById(R.id.galleryToolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(business.getName());
        getSupportActionBar().setSubtitle("Gallery");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.gallery_menu, menu);
        menu.findItem(R.id.action_add).setVisible(isEditable);
        menu.findItem(R.id.action_delete).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_add:
                addImageButton();
                break;
            case R.id.action_delete:
                deleteImagesButton();
                break;
            case R.id.action_logout:
                ((AppLoader)getApplicationContext()).logout(this);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void onGalleryUpdate() {
        final UploadBroadcastReceiver uploadReceiver = ((AppLoader)getApplicationContext()).getUploadReceiver();
        uploadReceiver.getNewImage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s == null || !business.getId().equals(uploadReceiver.getBusinessID()))
                    return;
                if(!uploadReceiver.isUploaded()){
                    int idx = business.getGallery().indexOf(s);
                    business.removeImage(s);
                    adapter.notifyItemRemoved(idx);
                    return;
                }
                if(uploadReceiver.isLogo()){
                    business.setLogo(s);
                    return;
                }
                business.addImage(s);
                adapter.notifyItemChanged(business.getGallery().indexOf(s));
                downloadImage(s);
            }
        });
    }

    private void downloadImages() {
        for(String image : business.getGallery()) {
            downloadImage(image);
        }
    }

    private void downloadImage(String image) {
        ImageDownloader.getImage(image, business.getId(), !isEditable, galleryFolder, this);
    }

    public void deleteImagesButton(){
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
                for(String image : adapter.getSelectedImages())
                    FirebaseHandler.getInstance().deleteImageForBusiness(business, image);
                if(business.getGallery().size() == 0)
                    findViewById(R.id.noGalleryTxt).setVisibility(View.VISIBLE);
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

    public void addImageButton() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
//        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
//        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), RC_ADD_IMAGES);
    }

    private void showMessage(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if(adapter.getIsSelecting()){
            adapter.triggerSelecting();
            return;
        }
        if(adapter.isOrderChanged()){
            FirebaseHandler.getInstance().updateGalleryForBusiness(business);
        }
        Intent backIntent = new Intent();
        setResult(RESULT_OK, backIntent);
        finish();
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

            for(int i=0;i<imageList.size();i++) {
                getContentResolver().takePersistableUriPermission(imageList.get(i), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                ImageUploader.addImageUpload(getApplicationContext(), business, imageList.get(i), false);
            }
            findViewById(R.id.noGalleryTxt).setVisibility(business.getGallery().size() > 0 ? View.GONE : View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

//    private void copyImageToAppFolder(Business business, Uri imageUri, String name) throws IOException {
//        File createDir = new File(getFilesDir(), business.getId());
//        if(!createDir.exists()) {
//            createDir.mkdir();
//        }
//        File file = new File(galleryFolder, name);
//        if(file.exists()){
//            return;
//        }
//
//        InputStream input = getContentResolver().openInputStream(imageUri);
//        Files.copy(input, file.toPath());
//        input.close();
//    }

    @Override
    public void onImageDownloaded(String businessID, final String imageName, boolean successful) {
        if(!business.getId().equals(businessID) || !successful)
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.addDownloadedImage(imageName);
            }
        });
    }
}