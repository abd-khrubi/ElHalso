package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ReviewsActivity extends AppCompatActivity {

    public class ReviewHolder extends RecyclerView.ViewHolder {
        private TextView usernameTxt;
        private RatingBar ratingBar;
        private TextView reviewTxt;
        public ReviewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTxt = itemView.findViewById(R.id.userNameTxt);
            ratingBar = itemView.findViewById(R.id.reviewRatingBar);
            reviewTxt = itemView.findViewById(R.id.reviewTxt);
        }

        public void setReview(Review review) {
            usernameTxt.setText(review.getUserName());
            ratingBar.setRating(review.getRating());
            reviewTxt.setText(review.getText());

        }
    }

    public class ReviewsAdapter extends RecyclerView.Adapter<ReviewHolder> {

        private ArrayList<Review> reviews;

        public ReviewsAdapter(ArrayList<Review> reviews) {
            this.reviews = reviews;
        }

        @NonNull
        @Override
        public ReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
            return new ReviewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewHolder holder, int position) {
            holder.setReview(reviews.get(position));
        }

        @Override
        public int getItemCount() {
            return reviews.size();
        }
    }

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
                logout();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void addReview(){

    }

    private void logout(){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Logout");
        alertDialog.setMessage("Are you sure you wish to logout?");
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Logout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(ReviewsActivity.this, LoginActivity.class);
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
}