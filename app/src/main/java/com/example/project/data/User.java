package com.example.project.data;

import java.util.ArrayList;

public class User {
    private String id;
    private String name;
    private String email;
    private String businessID;
    private ArrayList<String> favorites;
    private double radius;
    public User() { }

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public User(String id, String name, String email, String businessID, ArrayList<String> favorites) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.businessID = businessID;
        this.favorites = favorites;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getBusinessID() {
        return businessID;
    }

    public ArrayList<String> getFavorites() {
        return favorites == null ? new ArrayList<String>() : favorites;
    }

    public double getRadius() {
        return radius;
    }

    public void addFavoriteBusiness(String businessID){
        favorites = getFavorites();
        if(!favorites.contains(businessID))
            favorites.add(businessID);
    }

    public void removeFavoriteBusiness(String businessID){
        favorites = getFavorites();
        favorites.remove(businessID);
    }

    public void setBusinessID(String businessID) {
        this.businessID = businessID;
    }

    public void setFavorites(ArrayList<String> favorites) {
        this.favorites = favorites;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
