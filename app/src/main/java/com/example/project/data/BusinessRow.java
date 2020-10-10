package com.example.project.data;


public class BusinessRow {
    private String business_name;
    private float rating;
    private float distance;

    public BusinessRow(String b_name, float rat, float dist) {
        super();
        this.business_name = b_name;
        this.rating = rat;
        this.distance = dist;
    }

    public void setBusiness_name(String business_name) {
        this.business_name = business_name;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getBusiness_name() {
        return business_name;
    }

    public float getRating() {
        return rating;
    }

    public float getDistance() {
        return distance;
    }
}
