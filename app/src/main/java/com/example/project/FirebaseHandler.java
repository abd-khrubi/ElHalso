package com.example.project;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHandler {

    private static FirebaseHandler firebaseHandler;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private final MutableLiveData<Boolean> updateDone = new MutableLiveData<>();
    private Object objectToUpdate;

    private static final String BUSINESS = "business";
    private static final String USERS = "users";
    private static final String TAG = "FirebaseHandler";

    private FirebaseHandler() {
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
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
            DocumentReference docR = firestore.collection(BUSINESS).document();
            final Business business = new Business(docR.getId());
            // adding business
            docR.set(business).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    // adding businessID to user
                    firestore.collection(USERS).document(user.getId()).update("businessID", business.getId()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            user.setBusinessID(business.getId());
                            objectToUpdate = business;
                            updateDone.postValue(true);
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
                    Business business = task.getResult().toObject(Business.class);
                    objectToUpdate = business;
                    updateDone.postValue(true);
                }
                else {
                    Log.d(TAG, "Fetching user failed.");
                }
            }
        });
    }

//    private void fetchGalleryForBusiness(final Business business) {
//        storage.getReference().child(business.getId() + "/").listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
//            @Override
//            public void onComplete(@NonNull Task<ListResult> task) {
//                if(task.isSuccessful()){
//                    Log.d(TAG, "successfully retrieved gallery for business");
//                    int i=1;
//                    ArrayList<String> gallery = new ArrayList<>();
//                    for(StorageReference sr : task.getResult().getItems()){
//                        Log.d(TAG, i + "- " + sr.getName());
//                        gallery.add(sr.getName());
//                    }
//                    business.setGallery(gallery);
//                    objectToUpdate = business;
//                    updateDone.postValue(true);
//                }
//            }
//        });
//    }

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

    public void fetchNearbyBusinesses(GeoPoint myLocation, double distance){

    }

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

    public void addImageToBusinessGallery(final Business business, Uri image, final String imageName){
        // ToDo: make sure image name does not already exist (except when its 'logo')
        // adding image to storage
        storage.getReference().child(business.getId() + "/" + imageName).putFile(image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "file uploaded successfully");
                    business.addImage(imageName);
                    updateBusinessGallery(business);
                }
                else {
                    Log.d(TAG, "failed to upload file");
                }
            }
        });

    }

    public void deleteImageForBusiness(final Business business, final String image){
        storage.getReference().child(business.getId() + "/" + image).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "successfully deleted image from storage");
                    business.removeImage(image);
                    updateBusinessGallery(business);
                }
                else {
                    Log.d(TAG, "failed to delete image from storage");
                }
            }
        });
    }

    private void updateBusinessGallery(final Business business) {
        firestore.collection(BUSINESS).document(business.getId()).update("gallery", business.getGallery()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    objectToUpdate = business;
                    updateDone.postValue(true);
                }
                else {
                    Log.d(TAG, "failed to update business.");
                }
            }
        });
    }

    public void addReview(Business business, Review review) {
        business.addReview(review);
        updateBusinessReviews(business);
    }

    public void removeReview(Business business, Review review){
        business.removeReview(review);
        updateBusinessReviews(business);
    }

    private void updateBusinessReviews(final Business business){
        firestore.collection(BUSINESS).document(business.getId()).update("reviews", business.getReviews()).addOnCompleteListener(new OnCompleteListener<Void>() {
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
