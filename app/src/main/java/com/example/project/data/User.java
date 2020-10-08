package com.example.project.data;

import java.util.ArrayList;

public class User {
    private String id;
    private String name;
    private String email;
    private String businessID;
    private ArrayList<Business> favorites;
    private double radius;
    public User() { }

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public User(String id, String name, String email, String businessID, ArrayList<Business> favorites, double radius) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.businessID = businessID;
        this.favorites = favorites;
        this.radius = radius;
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

    public ArrayList<Business> getFavorites() {
        return favorites == null ? new ArrayList<>() : favorites;
    }

    public double getRadius() {
        return radius;
    }

    public void addFavoriteBusiness(Business business){
        favorites = getFavorites();
        if(!favorites.contains(business))
            favorites.add(business);
    }

    public void removeFavoriteBusiness(Business business){
        favorites = getFavorites();
        favorites.remove(business);
    }

    public void setBusinessID(String businessID) {
        this.businessID = businessID;
    }

    public void setFavorites(ArrayList<Business> favorites) {
        this.favorites = favorites;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
