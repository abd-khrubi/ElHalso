package com.example.project;

import java.util.ArrayList;

public class User {
    private String id;
    private String name;
    private String email;
    private String businessID;
    private ArrayList<String> favorites;

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

    public void setBusinessID(String businessID) {
        this.businessID = businessID;
    }

    public void setFavorites(ArrayList<String> favorites) {
        this.favorites = favorites;
    }
}