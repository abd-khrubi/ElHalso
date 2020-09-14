package com.example.project;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class FirebaseHandler {

    private FirebaseFirestore firestore;

    private final MutableLiveData<Business> business = new MutableLiveData<>();
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Business>> businessList = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Review>> review = new MutableLiveData<>();

    private static final String BUSINESS = "business";
    private static final String USERS = "users";
    private static final String TAG = "FirebaseHandler";

    public FirebaseHandler() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void fetchBusinessForUser(String UID) {
        firestore.collection(USERS).document(UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){

                }
                else {
                    Log.d(TAG, "Fetching user failed.");
                }
            }
        });
    }

    public ArrayList<Review> getReviewsForBusiness(String UID) {

    }

    public ArrayList<Business> getBusinessNear(Location loc){

    }

    public void updateBusiness(Business newBusiness){

    }

    public void addImageToBusinessGallery(String UID, String image){

    }

    public void deleteImageForBusiness(String UID, String image){

    }

    public void addReview(User user, Business business) {

    }

    public void deleteReview(Review rev){

    }

    public User getUser(String UID) {

    }

    public void updateUser(User newUser) {

    }

    public void addFavoriteBusiness(String UID, Business business) {

    }

    public void removeFavoriteBusiness(String UID, Business business) {

    }
}
