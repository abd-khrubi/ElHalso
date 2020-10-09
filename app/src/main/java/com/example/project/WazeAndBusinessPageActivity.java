package com.example.project;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project.GalleryActivity;
import com.example.project.R;
import com.example.project.data.Business;
import com.example.project.data.Review;
import com.google.common.io.Files;
import com.google.gson.Gson;

import java.io.File;

public class WazeAndBusinessPageActivity extends AppCompatActivity {

    private static final String TAG = "WazeAndBusinessPage";

    ImageButton waze;
    Business business;
    Button galleryBtn;
    TextView description;
    RatingBar starRating;
    TextView ratingAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waze_and_business_page);

        business = getIntent().getParcelableExtra("business");

        waze = findViewById(R.id.waze_btn);
        createWazeConnection(waze);

        galleryBtn = findViewById(R.id.gallery_btn);
        createGallery(galleryBtn, business);

        description = findViewById(R.id.tv_description);
        description.setText(business.getDescription());

        starRating = findViewById(R.id.rating_starts);
        starRating.setNumStars(5);
        starRating.setRating(business.getReviewsScore());
        starRating.setStepSize(0.5f);
        starRating.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            Log.d(TAG, "starRating.onRatingBarChangeListener: " + rating);
        });

        ratingAmount = findViewById(R.id.rating_amount_tv);
        ratingAmount.setText(String.format("%s Reviews", business.getReviews().size()));

    }

    void createGallery(Button galleryBtn, Business business) {
        galleryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, GalleryActivity.class);
            intent.putExtra("business", business);
            File galleryFolder = new File(Files.createTempDir(), business.getId());
            galleryFolder.mkdir();
            intent.putExtra("galleryFolder", galleryFolder.getAbsolutePath());
            startActivity(intent);
        });
    }


    void createWazeConnection(ImageButton waze) {
        waze.setOnClickListener(v -> {
            if (business.getCoordinates() == null) {
                Toast.makeText(this, "Location unavailable", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                String url = "https://waze.com/ul?ll=" + business.getCoordinates().getLatitude() + "%2C" +
                        business.getCoordinates().getLongitude() + "&navigate=yes";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                // If Waze is not installed, open it in Google Play:
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
                startActivity(intent);
            }
        });
    }
}