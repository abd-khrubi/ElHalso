package com.example.project;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.io.Files;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.File;

public class BusinessActivity extends AppCompatActivity implements ImageDownloader.DownloadCallback {

    private Business business;
    private RecyclerView galleryRecyclerView;
    private GalleryAdapter adapter;
    private File galleryFolder;
    private boolean ownedBusiness;
    private Menu menu;

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

        setSupportActionBar((Toolbar) findViewById(R.id.businessToolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(!ownedBusiness);
        getSupportActionBar().setTitle(business.getName());
        // todo: category subtitle?
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.business_menu, menu);
        menu.findItem(R.id.action_edit).setVisible(ownedBusiness);
        menu.findItem(R.id.action_favorite).setVisible(!ownedBusiness);
        if(!ownedBusiness) {
            User user = ((AppLoader)getApplicationContext()).getUser();
            int toDraw = user.getFavorites().contains(business.getId()) ? R.drawable.ic_is_favorite : R.drawable.ic_is_not_favorite;
            menu.findItem(R.id.action_favorite).setIcon(toDraw);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.home:
                onBackPressed();
                break;
            case R.id.action_edit:
                editBusiness();
                break;
            case R.id.action_favorite:
                toggleFavorite();
                break;
            case R.id.action_logout:
                logout();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    // todo: better as activity abstract class? no need?
    private void logout(){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Logout");
        alertDialog.setMessage("Are you sure you wish to logout?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(BusinessActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
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

    private void fillInBusinessDetails(){
        ((TextView)findViewById(R.id.descriptionTxt)).setText(business.getDescription() != null ? business.getDescription() : "(No description)");
        ((TextView)findViewById(R.id.reviewsTxt)).setText("(" + business.getReviews().size() + " reviews)");
        findViewById(R.id.noImagesTxt).setVisibility(business.getGallery().size() > 0 ? View.GONE : View.VISIBLE);
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

    public void editBusiness() {
        Intent intent = new Intent(this, EditBusinessActivity.class);
        startActivityForResult(intent, RC_EDIT_BUSINESS);
    }

    public void showReviews(View view){
        Intent intent = new Intent(this, ReviewsActivity.class);
        if(!ownedBusiness)
            intent.putExtra("business", business);
        startActivity(intent); // todo: activityForResult to update reviews count?
    }

    public void toggleFavorite() {
        Log.d(TAG, "toggling favorite");
        User user = ((AppLoader)getApplicationContext()).getUser();
        FirebaseHandler.getInstance().addFavoriteBusiness(user, business);
        int toDraw = user.getFavorites().contains(business.getId()) ? R.drawable.ic_is_favorite : R.drawable.ic_is_not_favorite;
        menu.findItem(R.id.action_favorite).setIcon(toDraw);
    }

    public void showGallery(View view) {
        Intent intent = new Intent(this, GalleryActivity.class);
        if(!ownedBusiness) {
            intent.putExtra("business", business);
            intent.putExtra("galleryFolder", galleryFolder.getAbsolutePath());

        }
        startActivity(intent);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_EDIT_BUSINESS && resultCode == RESULT_OK){
            fillInBusinessDetails();
            downloadImages();
        }
    }
}