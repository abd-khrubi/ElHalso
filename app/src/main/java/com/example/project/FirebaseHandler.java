package com.example.project;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHandler {

    private FirebaseFirestore firestore;

    private final MutableLiveData<Business> business = new MutableLiveData<>();
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Business>> businessList = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Review>> review = new MutableLiveData<>();
    private final MutableLiveData<Boolean> update = new MutableLiveData<>();

    private static final String BUSINESS = "business";
    private static final String USERS = "users";
    private static final String TAG = "FirebaseHandler";

    public FirebaseHandler() {
        firestore = FirebaseFirestore.getInstance();
    }

    public LiveData<Boolean> getUpdate(){
        update.setValue(false);
        return update;
    }

    public void fetchBusinessForUser(final User user) {
        Log.d(TAG, "starting");

        // user starting new business
        if(user.getBusinessID() == null) {
            final Business bus = new Business();
            DocumentReference docR = firestore.collection(BUSINESS).document();
            bus.setId(docR.getId());
            // adding business
            docR.set(bus).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // adding businessID to user
                    firestore.collection(USERS).document(user.getId()).update("businessID", bus.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            user.setBusinessID(bus.getId());
                            update.postValue(true);
                            Log.d(TAG, "update was " + task.isSuccessful());
                            if(!task.isSuccessful()){
                                Log.d(TAG, "failed: " + task.getException().toString());
                            }
                        }
                    });
                }
            });

            business.postValue(bus);
            return;
        }

        // user already has a business
        firestore.collection(BUSINESS).document(user.getBusinessID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot snap = task.getResult();
                    Business temp = snap.toObject(Business.class);
                    business.postValue(snap.toObject(Business.class));
                }
                else {
                    Log.d(TAG, "Fetching user failed.");
                }
            }
        });
    }

    public void updateOrCreateFirebaseUser(final User user) {

        firestore.collection(USERS).document(user.getId()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot snap = task.getResult();
                    if(snap.exists()) {
                        user.setToOtherUser(snap.toObject(User.class));
                        update.postValue(true);
                    }
                    else {
                        firestore.collection(USERS).document(user.getId()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful())
                                    update.postValue(true);
                                else
                                    Log.d(TAG, "failed to create user");
                            }
                        });
                    }
                }
                else {
                    Log.d(TAG, "failed to get user");
                }
            }
        });
    }

//    public ArrayList<Review> getReviewsForBusiness(String UID) {
//
//    }
//
//    public ArrayList<Business> getBusinessNear(Location loc){
//
//    }
//
//    public void updateBusiness(Business newBusiness){
//
//    }
//
//    public void addImageToBusinessGallery(String UID, String image){
//
//    }
//
//    public void deleteImageForBusiness(String UID, String image){
//
//    }
//
//    public void addReview(User user, Business business) {
//
//    }
//
//    public void deleteReview(Review rev){
//
//    }
//
//    public User getUser(String UID) {
//
//    }

    public void updateUser(User newUser) {

    }

    public void addFavoriteBusiness(String UID, Business business) {

    }

    public void removeFavoriteBusiness(String UID, Business business) {

    }
}
