package com.example.project.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.data.Review;

import java.util.ArrayList;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewHolder> {

    private ArrayList<Review> reviews;

    public ReviewsAdapter(ArrayList<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.review_item, parent, false);
        view.findViewById(R.id.reviewTxt).setOnClickListener(v -> {
            int maxLines = v.getResources().getInteger(R.integer.review_text_default_lines);
            if(((TextView)v).getMaxLines() == maxLines) {
                ((TextView)v).setMaxLines(Integer.MAX_VALUE);
            }
            else {
                ((TextView)v).setMaxLines(maxLines);
            }
        });
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

    public static class ReviewHolder extends RecyclerView.ViewHolder {
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
            String reviewText = review.getText();
            if(reviewText == null || reviewText.trim().equals("")){
                reviewText = "(No text)";
                reviewTxt.setAlpha(0.7f);
            }
            else {
                reviewTxt.setAlpha(1f);
            }
            reviewTxt.setText(reviewText);
        }
    }

}