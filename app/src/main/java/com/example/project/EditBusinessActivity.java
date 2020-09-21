package com.example.project;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

public class EditBusinessActivity extends AppCompatActivity {

    private Business business;
    private String logoLocation;
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
    }

    private void fillInBusinessDetails(Bundle savedInstanceState){
        logoImg = (ImageButton) findViewById(R.id.logoImgBtn);
        nameTxt = (EditText) findViewById(R.id.nameTxt);
        descriptionTxt = (EditText) findViewById(R.id.descriptionTxt);

        if(savedInstanceState == null){
            // ToDo if rotation
            return;
        }

        nameTxt.setText(business.getName() == null ? "" : business.getName());
        descriptionTxt.setText(business.getDescription() == null ? "" : business.getDescription());
    }

    public void saveBusiness(View view) {
        if(!validateDetails()) {
            return;
        }
        ImageButton logoImg = (ImageButton) findViewById(R.id.logoImgBtn);
        EditText nameTxt = (EditText) findViewById(R.id.nameTxt);
        EditText descriptionTxt = (EditText) findViewById(R.id.descriptionTxt);

        business.setName(nameTxt.getText().toString());
        business.setDescription(descriptionTxt.getText().toString());
        business.setLogo(logoLocation);
        // todo: update firebase
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
        intent.putExtra("gallery", business.getGallery());

        startActivityForResult(intent, RC_EDIT_GALLERY);
    }

    public void setLocationButton(View view) {
        // open map
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_EDIT_GALLERY && resultCode == RESULT_OK) {

        }
    }
}