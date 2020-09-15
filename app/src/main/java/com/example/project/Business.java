package com.example.project;

import java.util.ArrayList;

public class Business {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String description;
    private ArrayList<String> gallery;
    private ArrayList<String> reviews;

    public Business() { }

    public Business(String id, String name, double latitude, double longitude, String description, ArrayList<String> gallery, ArrayList<String> reviews) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getGallery() {
        return gallery;
    }

    public ArrayList<String> getReviews() {
        return reviews;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
