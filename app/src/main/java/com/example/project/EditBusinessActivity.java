package com.example.project;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class EditBusinessActivity extends AppCompatActivity {

    private Business business;
    private String logoName;
    private ImageButton logoImg;
    private EditText nameTxt;
    private EditText descriptionTxt;
    private static final int RC_EDIT_GALLERY = 481;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_business);
        business = ((AppLoader) getApplicationContext()).getBusiness();
        fillInBusinessDetails(savedInstanceState);
        Log.d("EditBus", business.getId());
    }

    private void fillInBusinessDetails(Bundle savedInstanceState){
        logoImg = (ImageButton) findViewById(R.id.logoImgBtn);
        nameTxt = (EditText) findViewById(R.id.nameTxt);
        descriptionTxt = (EditText) findViewById(R.id.descriptionTxt);

        logoName = business.getLogo();
        if(savedInstanceState == null){
            // ToDo if rotation
            return;
        }

        nameTxt.setText(business.getName() == null ? "" : business.getName());
        descriptionTxt.setText(business.getDescription() == null ? "" : business.getDescription());
        if(business.getLogo() != null) {
            File galleryFolder = new File(getFilesDir(), business.getId());
            File logo = new File(galleryFolder, logoName);
            Picasso.get().load(Uri.fromFile(logo)).fit().into(logoImg);
        }
    }

    public void saveBusiness(View view) {
        if(!validateDetails()) {
            return;
        }

        if(detailsChanged()){
            finish();
            return;
        }

        business.setName(nameTxt.getText().toString());
        business.setDescription(descriptionTxt.getText().toString());
        business.setLogo(logoName);
        // todo: update firebase
    }

    public void changeLogoButton(View view) {
        // todo: change image to picked image, copy to folder when saved
    }

    private boolean detailsChanged() {
        return !business.getName().equals(nameTxt.getText().toString())
                && !business.getDescription().equals(descriptionTxt.getText().toString())
                && (logoName == null || !business.getLogo().equals(logoName));
    }

    private boolean validateDetails(){
        if(nameTxt.getText().toString().trim().length() < 3){
            showMessage("Business name is too short.");
            return false;
        }

        return true;
    }

    private void showMessage(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void editGalleryButton(View view) {
        Intent intent = new Intent(this, GalleryActivity.class);
        intent.putExtra("mode", "editable");
        File galleryFolder = new File(getFilesDir(), business.getId());
        if(!galleryFolder.exists())
            galleryFolder.mkdir();
        intent.putExtra("gallery_folder", galleryFolder.getAbsolutePath());
        intent.putExtra("gallery", business.getGallery());
        intent.putExtra("businessID", business.getId());

        startActivityForResult(intent, RC_EDIT_GALLERY);
    }

    public void setLocationButton(View view) {
        // open map
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_EDIT_GALLERY && resultCode == RESULT_OK) {
            ArrayList<String> newGallery = data.getStringArrayListExtra("gallery");
            boolean changed = newGallery.size() != business.getGallery().size();
            for(int i=0;i<newGallery.size();i++){
                if(changed || !business.getGallery().get(i).equals(newGallery.get(i))) {
                    changed = true;
                    break;
                }
            }
            if(changed)
                business.setGallery(newGallery);
        }
    }
}