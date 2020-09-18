package com.example.project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class BusinessActivity extends AppCompatActivity {

    private Business business;
    private static final int RC_EDIT_BUSINESS = 974;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business);
        business = ((AppLoader) getApplicationContext()).getBusiness();
    }

    public void editBusiness(View view) {
        Intent intent = new Intent(this, EditBusinessActivity.class);
        startActivityForResult(intent, RC_EDIT_BUSINESS);
    }
}