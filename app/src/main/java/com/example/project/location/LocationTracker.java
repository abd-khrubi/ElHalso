package com.example.project.location;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import java.util.ArrayList;
import java.util.List;

public class LocationTracker extends LocationCallback {

    private static final String TAG = "LocationTracker";

    private Context context;
    private List<LocationReceivedCallback> callbacks;

    private boolean trackerReady = false;
    private boolean tracking = false;
    private LocationInfo lastLocation = null;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;

    public LocationTracker(Context context) {
        super();
        this.context = context;
        this.callbacks = new ArrayList<>();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        createLocationRequest();
    }

    @Override
    public void onLocationResult(LocationResult locationResult) {
        if (locationResult == null) {
            return;
        }

        for (Location location : locationResult.getLocations()) {
            LocationInfo loc = new LocationInfo(location.getLatitude(), location.getLongitude(), location.getAccuracy());
            Log.i(TAG, "onLocationResult: Got location: " + loc);

            if (loc != lastLocation) {
                lastLocation = loc;
                for (LocationReceivedCallback callback : callbacks) {
                    callback.onLocationReceived(loc);
                }
            }
        }
    }

    public void startTracking() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
//            ActivityCompat.requestPermissions((Activity) context, new String[]{
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//            }, 101); // TODO move this out of here
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, this, Looper.getMainLooper())
                .addOnSuccessListener(aVoid -> {
                    // TODO
                })
                .addOnFailureListener(e -> {
                    // TODO
                });
        tracking = true;
    }

    public void stopTracking() {
        fusedLocationProviderClient.removeLocationUpdates(this)
                .addOnSuccessListener(aVoid -> {

                });
        tracking = false;
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create()
                .setInterval(10000) // 10 seconds
                .setFastestInterval(5000) // 5 seconds
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(context);
        client.checkLocationSettings(builder.build())
                .addOnSuccessListener(it -> {
                    trackerReady = true;
                }).addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ((ResolvableApiException) e).startResolutionForResult((Activity) context, 1);
                } catch (IntentSender.SendIntentException ignored) {}
            }
        });
    }

    public boolean isTrackerReady() {
        return trackerReady && lastLocation != null;
    }

    public boolean isTracking() {
        return tracking;
    }

    public LocationInfo getLastLocation() {
        return lastLocation;
    }

    public void registerCallback(LocationReceivedCallback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    public void clearCallback(LocationReceivedCallback callback) {
        callbacks.remove(callback);
    }
}

