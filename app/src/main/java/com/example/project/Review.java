package com.example.project;

public class Review {
    private String businessID;
    private String  userID;
    private String userName;
    private int rating;
    private String text;

    public Review() {}

    public Review(String businessID, String userID, String userName, int rating, String text) {
        this.businessID = businessID;
        this.userID = userID;
        this.userName = userName;
        this.rating = rating;
        this.text = text;
    }

    public String getBusinessID() {
        return businessID;
    }

    public String getText() {
        return text;
    }

    public int getRating() {
        return rating;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }
}
