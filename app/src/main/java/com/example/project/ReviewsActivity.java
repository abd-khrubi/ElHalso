package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.project.adapters.ReviewsAdapter;
import com.example.project.data.Business;
import com.example.project.data.Review;

import java.util.ArrayList;

public class ReviewsActivity extends AppCompatActivity {


    private Business business;
    private boolean ownedBusiness;
    private RecyclerView reviewsRecyclerView;
    private ReviewsAdapter adapter;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        if(getIntent().hasExtra("business")){
            business = getIntent().getParcelableExtra("business");
            ownedBusiness = false;
        }
        else {
            business = ((AppLoader)getApplication()).getBusiness();
            ownedBusiness = true;
        }
        findViewById(R.id.noReviewsTxt).setVisibility(business.getReviews().size() > 0 ? View.GONE : View.VISIBLE);

        reviewsRecyclerView = (RecyclerView) findViewById(R.id.reviewsRecyclerView);
        adapter = new ReviewsAdapter(business.getReviews());
        reviewsRecyclerView.setAdapter(adapter);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        setSupportActionBar((Toolbar) findViewById(R.id.reviewsToolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(business.getName());
        getSupportActionBar().setSubtitle("Reviews");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.reviews_menu, menu);
        menu.findItem(R.id.action_add_review).setVisible(!ownedBusiness);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_add_review:
                addReview();
                break;
            case R.id.action_logout:
                ((AppLoader)getApplicationContext()).logout(this);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void addReview(){

    }
}