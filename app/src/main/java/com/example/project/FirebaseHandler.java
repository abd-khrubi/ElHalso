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
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHandler {

    private static FirebaseHandler firebaseHandler;
    private FirebaseFirestore firestore;

    private final MutableLiveData<Business> business = new MutableLiveData<>();
    private final MutableLiveData<User> user = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Business>> businessList = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Review>> review = new MutableLiveData<>();
    private final MutableLiveData<Boolean> updateDone = new MutableLiveData<>();
    private Object objectToUpdate;

    private static final String BUSINESS = "business";
    private static final String USERS = "users";
    private static final String TAG = "FirebaseHandler";

    private FirebaseHandler() {
        firestore = FirebaseFirestore.getInstance();
    }

    public static FirebaseHandler getInstance(){
        if(firebaseHandler == null)
            firebaseHandler = new FirebaseHandler();

        return firebaseHandler;
    }

    public LiveData<Boolean> getUpdate(){
        updateDone.setValue(false);
        objectToUpdate = null;
        return updateDone;
    }

    public Object getUpdatedObject() {
        return objectToUpdate;
    }

    public void fetchBusinessForUser(final User user) {
        Log.d(TAG, "starting");

        // user starting new business
        if(user.getBusinessID() == null) {
            final Business bus = new Business();
            DocumentReference docR = firestore.collection(BUSINESS).document();
            bus.setId(docR.getId());
            objectToUpdate = bus;
            // adding business
            docR.set(bus).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // adding businessID to user
                    firestore.collection(USERS).document(user.getId()).update("businessID", bus.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            user.setBusinessID(bus.getId());
                            updateDone.postValue(true);
                            Log.d(TAG, "update was " + task.isSuccessful());
                            if(!task.isSuccessful()){
                                Log.d(TAG, "failed: " + task.getException().toString());
                            }
                        }
                    });
                }
            });
            return;
        }

        // user already has a business
        firestore.collection(BUSINESS).document(user.getBusinessID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    objectToUpdate = task.getResult().toObject(Business.class);
                    updateDone.postValue(true);
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
                        User userFetched = snap.toObject(User.class);
                        user.setBusinessID(userFetched.getBusinessID());
                        user.setFavorites(userFetched.getFavorites());
                        objectToUpdate = user;
                        updateDone.postValue(true);
                    }
                    else {
                        firestore.collection(USERS).document(user.getId()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()) {
                                    objectToUpdate = user;
                                    updateDone.postValue(true);
                                }
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
//
//    public ArrayList<Business> getBusinessNear(Location loc){
//
//    }
//
    public void updateBusiness(final Business newBusiness){
        firestore.collection(BUSINESS).document(newBusiness.getId()).set(newBusiness).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    objectToUpdate = newBusiness;
                    updateDone.postValue(true);
                }
                else {
                    Log.d(TAG, "failed to update business.");
                }
            }
        });
    }
//
//    public void addImageToBusinessGallery(Business UID, String image){
//
//    }
//
//    public void deleteImageForBusiness(Business UID, String image){
//
//    }
//
    public void addReview(final Business business, final Review review) {
        ArrayList<Review> reviews = business.getReviews();
        reviews.add(review);
        firestore.collection(BUSINESS).document(business.getId()).update("reviews", reviews).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    objectToUpdate = business;
                    updateDone.postValue(true);
                }
                else {
                    Log.d(TAG, "failed to add review");
                }
            }
        });
    }
//
    public void deleteReview(final Business business, final Review review){
        ArrayList<Review> reviews = business.getReviews();
        reviews.remove(review);
        firestore.collection(BUSINESS).document(business.getId()).update("reviews", reviews).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    objectToUpdate = business;
                    updateDone.postValue(true);
                }
                else {
                    Log.d(TAG, "failed to add review");
                }
            }
        });
    }
//
//    public User getUser(String UID) {
//
//    }

//    public void updateUser(User newUser) {
//
//    }

    public void addFavoriteBusiness(User user, Business business) {
        ArrayList<String> favorites = user.getFavorites();
        favorites.add(business.getId());
        updateUserFavorites(user, favorites);
    }

    public void removeFavoriteBusiness(User user, Business business) {
        ArrayList<String> favorites = user.getFavorites();
        favorites.remove(business.getId());
        updateUserFavorites(user, favorites);
    }

    private void updateUserFavorites(User user, ArrayList<String> favorites) {
        firestore.collection(USERS).document(user.getId()).update("favorites", favorites).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    updateDone.postValue(true);
                }
                else {
                    Log.d(TAG, "adding favorite failed");
                }
            }
        });
    }
}
