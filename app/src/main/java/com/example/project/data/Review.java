package com.example.project.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Review implements Parcelable {
    private String businessID;
    private String  userID;
    private String userName;
    private float rating;
    private String text;

    public Review() {}

    public Review(String businessID, String userID, String userName, float rating, String text) {
        this.businessID = businessID;
        this.userID = userID;
        this.userName = userName;
        this.rating = rating;
        this.text = text;
    }

    protected Review(Parcel in) {
        businessID = in.readString();
        userID = in.readString();
        userName = in.readString();
        rating = in.readInt();
        text = in.readString();
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    public String getBusinessID() {
        return businessID;
    }

    public String getText() {
        return text;
    }

    public float getRating() {
        return rating;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(businessID);
        dest.writeString(userID);
        dest.writeString(userName);
        dest.writeFloat(rating);
        dest.writeString(text);
    }
}
