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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.toRadians;

public class FirebaseHandler {

    private static FirebaseHandler firebaseHandler;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private final MutableLiveData<Boolean> updateDone = new MutableLiveData<>();
    private Object objectToUpdate;

    private static final String BUSINESS = "business";
    private static final String USERS = "users";
    private static final String TAG = "FirebaseHandler";
    private static final String LOCAL_DOWNLOAD_FOLDER = "myproject";

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

    public void fetchNearbyBusinesses(final GeoPoint myLocation, final double distance){
//        final GeoPoint orig = calculateGeopointAtDistanceFrom(myLocation, 0, 0);
//        GeoPoint north = calculateGeopointAtDistanceFrom(orig, distance, 0);
//        GeoPoint south = calculateGeopointAtDistanceFrom(orig, distance, 180);
//        GeoPoint east = calculateGeopointAtDistanceFrom(orig, distance, 90);
//        GeoPoint west = calculateGeopointAtDistanceFrom(orig, distance, 270);
//
//        Log.d(TAG, "MyLoc:" + orig.toString());
//        Log.d(TAG, "West:" + north.toString());
//        Log.d(TAG, "East:" + north.toString());
//        Log.d(TAG, "South:" + south.toString());
//        Log.d(TAG, "North:" + north.toString());


        firestore.collection(BUSINESS).whereGreaterThanOrEqualTo("name", "")
//                .whereGreaterThanOrEqualTo("coordinates", west)
//                .whereLessThanOrEqualTo("coordinates", east)
//                .whereGreaterThanOrEqualTo("coordinates", south)
//                .whereLessThanOrEqualTo("coordinates", north)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()) {
                    Log.d(TAG, "successfully queried " + task.getResult().size() + " businesses");
                    ArrayList<Business> businesses = new ArrayList<>();
                    for(QueryDocumentSnapshot doc : task.getResult()){
                        if(calculateDistance(myLocation, doc.getGeoPoint("coordinates")) <= distance*1000) {
                            businesses.add(doc.toObject(Business.class));
                        }
                    }
                    objectToUpdate = businesses;
                    updateDone.postValue(true);
                }
                else {
                    Log.d(TAG, "failed to query");
                }
            }
        });
    }

    private double calculateDistance(GeoPoint point1, GeoPoint point2) {
        float[] result = new float[1];
//        Location.distanceBetween(Math.toRadians(point1.getLatitude()), Math.toRadians(point1.getLongitude()), Math.toRadians(point2.getLatitude()), Math.toRadians(point2.getLongitude()), result);
        Location.distanceBetween(point1.getLatitude(), point1.getLongitude(), point2.getLatitude(), point2.getLongitude(), result);
        return result[0];
    }

    private GeoPoint calculateGeopointAtDistanceFrom(GeoPoint location, double distance, double bearing) {
//        double dist = distance/6371.0;
//        double brng = Math.toRadians(bearing);
//        double lat1 = Math.toRadians(location.getLatitude());
//        double lon1 = Math.toRadians(location.getLongitude());
//
//        double lat2 = Math.asin( Math.sin(lat1)*Math.cos(dist) + Math.cos(lat1)*Math.sin(dist)*Math.cos(brng) );
//        double a = Math.atan2(Math.sin(brng)*Math.sin(dist)*Math.cos(lat1), Math.cos(dist)-Math.sin(lat1)*Math.sin(lat2));
//        double lon2 = lon1 + a;
//
//        lon2 = (lon2+ 3*Math.PI) % (2*Math.PI) - Math.PI;
//        return new GeoPoint(Math.toDegrees(lat2), Math.toDegrees(lon2));

        double brngRad = Math.toRadians(bearing);
        double latRad = Math.toRadians(location.getLatitude());
        double lonRad = Math.toRadians(location.getLongitude());
        double distFrac = distance / 6371.0;

        double latitudeResult = Math.asin(Math.sin(latRad) * Math.cos(distFrac) + Math.cos(latRad) * Math.sin(distFrac) * Math.cos(brngRad));
        double a = Math.atan2(Math.sin(brngRad) * Math.sin(distFrac) * Math.cos(latRad), Math.cos(distFrac) - Math.sin(latRad) * Math.sin(latitudeResult));
        double longitudeResult = (lonRad + a + 3 * Math.PI) % (2 * Math.PI) - Math.PI;
        return new GeoPoint(Math.toDegrees(latitudeResult), Math.toDegrees(longitudeResult));
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

    public void addImageToBusinessGallery(final Business business, Uri image, String imageName){
        // ToDo: make better sure image name does not already exist (except when its 'logo')
        if(business.getGallery().contains(imageName)){
            int idx = imageName.lastIndexOf('.');
            imageName = imageName.substring(0,idx) + "_1" + imageName.substring(idx);
        }
        final String name = imageName;
        // adding image to storage
        storage.getReference().child(business.getId() + "/" + imageName).putFile(image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "file uploaded successfully");
                    business.addImage(name);
                    objectToUpdate = name;
                    updateBusinessGallery(business);
                }
                else {
                    Log.d(TAG, "failed to upload file");
                }
            }
        });
    }

//    public void addImagesToBusinessGallery(final Business business, ArrayList<Uri> images, final String imageName){
//        // ToDo: make sure images name does not already exist (except when its 'logo')
//        // adding image to storage
//        storage.getReference().child(business.getId() + "/" + imageName).putFile(image).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                if(task.isSuccessful()){
//                    Log.d(TAG, "file uploaded successfully");
//                    business.addImage(imageName);
//                    updateBusinessGallery(business);
//                }
//                else {
//                    Log.d(TAG, "failed to upload file");
//                }
//            }
//        });
//    }

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

    public void fetchImageForBusiness(Business business, String image, File appDir) {
        if(image.charAt(0) == '#') {
            updateDone.postValue(true);
            return;
        }
        File folder = new File(appDir, business.getId());
        if(!folder.exists()) {
            folder.mkdir();
        }
        final File localFile = new File(folder, image);
        if(localFile.exists()){
            updateDone.postValue(true);
            return;
        }

        try {
            localFile.createNewFile();
        } catch (IOException e) {
            Log.d(TAG, "failed to create file");
            Log.d(TAG, e.toString());
        }
        storage.getReference().child(business.getId() + "/" + image).getFile(localFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "image downloaded successfully");
                    updateDone.postValue(true);
                }
                else {
                    Log.d(TAG, "image failed to download");
                    updateDone.postValue(true);
                }
            }
        });
    }

    private void updateBusinessGallery(final Business business) {
        firestore.collection(BUSINESS).document(business.getId()).update("gallery", business.getGallery()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
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
