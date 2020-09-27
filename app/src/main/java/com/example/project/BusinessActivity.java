package com.example.project;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.io.Files;
import com.squareup.picasso.Picasso;

import java.io.File;

public class BusinessActivity extends AppCompatActivity implements ImageDownloader.DownloadCallback {

    private Business business;
    private RecyclerView galleryRecyclerView;
    private GalleryAdapter adapter;
    private File galleryFolder;
    private boolean ownedBusiness;

    private static final float EMPTY_TEXT_ALPHA = 0.6f;

    private static final int RC_EDIT_BUSINESS = 974;
    private static final int RC_READ_EXTERNAL_PERMISSION = 675;

    private static final String TAG = "BusinessActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);
        setupBusiness(getIntent());
        fillInBusinessDetails();

        Log.d(TAG, "in " + business.getId());
        galleryRecyclerView = (RecyclerView) findViewById(R.id.galleryRecyclerView);
        adapter = new GalleryAdapter(this, business.getGallery(), galleryFolder, false, null);
        galleryRecyclerView.setAdapter(adapter);
        galleryRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        onBusinessUpdate();

        downloadImages();

//        boolean hasPermission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//        if(!hasPermission) {
//            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, RC_READ_EXTERNAL_PERMISSION);
//        }
    }

//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == RC_READ_EXTERNAL_PERMISSION) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Log.d(TAG, "got permission");
//            } else {
//                AlertDialog alertDialog = new AlertDialog.Builder(this).create();
//                alertDialog.setTitle("Permission required");
//                alertDialog.setMessage("");
//                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                alertDialog.show();
//            }
//        }
//    }

    private void fillInBusinessDetails(){
        ((TextView)findViewById(R.id.nameTxt)).setText(business.getName() != null ? business.getName() : "");
        ((TextView)findViewById(R.id.descriptionTxt)).setText(business.getDescription() != null ? business.getDescription() : "(No description)");
        ((TextView)findViewById(R.id.reviewsTxt)).setText("(" + business.getReviews().size() + " reviews)");
        if(!ownedBusiness){
            User user = ((AppLoader)getApplicationContext()).getUser();
            int toDraw = user.getFavorites().contains(business.getId()) ? R.drawable.ic_is_favorite : R.drawable.ic_is_not_favorite;
            ((ImageView)findViewById(R.id.editBtn)).setImageDrawable(getDrawable(toDraw));
        }
        setRatingBar();
    }

    private void onBusinessUpdate() {
        final UploadBroadcastReceiver uploadReceiver = ((AppLoader)getApplicationContext()).getUploadReceiver();
        uploadReceiver.getNewImage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s == null || !business.getId().equals(uploadReceiver.getBusiness().getId()))
                    return;
                if(uploadReceiver.isLogo()) {
                    business.setLogo(s);
                }
                else {
                    business.addImage(s);
                    adapter.notifyItemInserted(business.getGallery().indexOf(s));
                }
                downloadImages();
                fillInBusinessDetails();
            }
        });
    }

    private void downloadImages() {
        if(business.getLogo() != null) {
            Log.d(TAG, "adding logo <"+business.getLogo()+">");
            ImageDownloader.getImage(business.getLogo(), business.getId(), !ownedBusiness, galleryFolder, this);
        }
        for(String image : business.getGallery()){
            ImageDownloader.getImage(image, business.getId(), !ownedBusiness, galleryFolder, this);
        }
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
            galleryFolder.deleteOnExit();
        }

        findViewById(R.id.editBtn).setVisibility(ownedBusiness ? View.VISIBLE : View.GONE);
        findViewById(R.id.toggleFavoriteBtn).setVisibility(!ownedBusiness ? View.VISIBLE : View.GONE);

    }

    private void setupDescription(){
        TextView descriptionView = findViewById(R.id.descriptionTxt);
        String description = business.getDescription();
        descriptionView.setText(description);
        Log.d(TAG,descriptionView.getLineCount() + "");
        descriptionView.setText(description != null ? business.getDescription() : "(No description)");
        if(description == null || description.trim().equals("")){
            descriptionView.setAlpha(EMPTY_TEXT_ALPHA);
        }

//        Log.d(TAG, descriptionView.getLayout().getLineEnd(3) + " in line 3");
    }

    private void setRatingBar(){
        float sum = 0;
        for(Review review : business.getReviews()){
            sum += review.getRating();
        }
        ((RatingBar)findViewById(R.id.ratingBar)).setRating(sum / business.getReviews().size());
    }

    public void editBusiness(View view) {
        Intent intent = new Intent(this, EditBusinessActivity.class);
        startActivityForResult(intent, RC_EDIT_BUSINESS);
    }

    public void showReviews(View view){

    }

    public void toggleFavorite(View view) {

    }

    @Override
    public synchronized void onImageDownloaded(String businessID, final String imageName) {
        if(!business.getId().equals(businessID))
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(business.getLogo() != null && imageName.equals(business.getLogo())){
                    ImageView image = findViewById(R.id.logoImg);
                    File imageFile = new File(galleryFolder, imageName);
                    Picasso.get().load(Uri.fromFile(imageFile)).fit().into(image);
                    return;
                }
                adapter.addDownloadedImage(imageName);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(ownedBusiness)
            return;
        for(File image : galleryFolder.listFiles()){
            image.delete();
        }
        galleryFolder.delete();
    }
}