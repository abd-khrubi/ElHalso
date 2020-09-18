package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class EditBusinessActivity extends AppCompatActivity {

    private Business business;
    private String logoLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_business);
        business = ((AppLoader) getApplicationContext()).getBusiness();
        fillInBusinessDetails();
    }

    private void fillInBusinessDetails(){
        ImageButton logoImg = (ImageButton) findViewById(R.id.logoImgBtn);
        EditText nameTxt = (EditText) findViewById(R.id.nameTxt);
        EditText descriptionTxt = (EditText) findViewById(R.id.descriptionTxt);

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
    }

    private boolean validateDetails(){
        return true;
    }

    private void showMessage(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void editGalleryButton(View view) {

    }

    public void setLocationButton(View view) {

    }
}