package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

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

        reviewsRecyclerView = (RecyclerView) findViewById(R.id.reviewsRecyclerView);
        adapter = new ReviewsAdapter(business.getReviews());
        reviewsRecyclerView.setAdapter(adapter);
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        findViewById(R.id.addReviewBtn).setVisibility(!ownedBusiness ? View.VISIBLE : View.GONE);
        ((TextView)findViewById(R.id.nameTxt)).setText(business.getName());
    }

    public void addReviewButton(View view) {
        // todo: dialog/activity to add review
    }
}