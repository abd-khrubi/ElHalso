package com.example.project;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class Business {
    private String id;
    private String name;
    private GeoPoint coordinates;
    private String description;
    private ArrayList<String> gallery;
    private ArrayList<Review> reviews;

    public Business() { }

    public Business(String id) {
        this.id = id;
    }

    public Business(String id, String name, GeoPoint coordinates, String description, ArrayList<String> gallery, ArrayList<Review> reviews) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.description = description;
        this.gallery = gallery;
        this.reviews = reviews;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public GeoPoint getCoordinates() {
        return coordinates;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getGallery() {
        return gallery == null ? new ArrayList<String>() : gallery;
    }

    public ArrayList<Review> getReviews() {
        return reviews == null ? new ArrayList<Review>() : reviews;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addReview(Review review) {
        reviews = getReviews();
        reviews.add(review);
    }

    public void removeReview(Review review) {
        reviews = getReviews();
        reviews.remove(review);
    }

    public void addImage(String image) {
        gallery = getGallery();
        gallery.add(image);
    }

    public void removeImage(String image) {
        gallery = getGallery();
        gallery.remove(image);
    }
}
