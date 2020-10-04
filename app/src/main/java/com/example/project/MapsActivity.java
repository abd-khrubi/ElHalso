package com.example.project;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;

import com.example.project.location.LocationInfo;
import com.example.project.location.LocationTracker;
import com.example.project.marker.MapMarkerAdapter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "MapsActivity";


    private GoogleMap mMap;

    List<Business> businesses = new ArrayList<>();
    private LocationTracker locationTracker;
    private boolean running = false;
    private boolean shouldAsk = true;
    private LocationInfo currentLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // location tracking
        locationTracker = new LocationTracker(this, locationInfo -> {
            if (locationTracker.isTrackerReady() && locationTracker.isTracking()) {
                currentLocation = locationInfo;
                mMap.animateCamera(CameraUpdateFactory.newLatLng(locationInfo.toLatLng()));
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.builder()
//                .target(sydney)
//                .zoom(15)
//                .build()));

        if (currentLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLocation.toLatLng()));
        }

    }

    private CameraUpdate getZoomForDistance(LatLng originalPosition, double distance) {
        distance *= 1.5;
        LatLng rightBottom = SphericalUtil.computeOffset(originalPosition, distance, 135);
        LatLng leftTop = SphericalUtil.computeOffset(originalPosition, distance, -45);
        LatLngBounds sBounds = new LatLngBounds(new LatLng(rightBottom.latitude, leftTop.longitude), new LatLng(leftTop.latitude, rightBottom.longitude));
        return CameraUpdateFactory.newLatLngBounds(sBounds, 0);

    }

    public void tstButton(View view) {
        Log.i(TAG, "tstButton: Click!!");
        if (!locationTracker.isTrackerReady()) {
            return;
        }
        final FirebaseHandler firebaseHandler = FirebaseHandler.getInstance();
        mMap.animateCamera(getZoomForDistance(currentLocation.toLatLng(), 100));
        final Business[] business = new Business[1];
        firebaseHandler.fetchNearbyBusinesses(currentLocation.toGeoPoint(), 100);
        LiveData<Boolean> updateDone = firebaseHandler.getUpdate();
        updateDone.observe(this, value -> {
            if (!value) {
                return;
            }
            updateDone.removeObservers(this);
            //noinspection unchecked
            businesses = (ArrayList<Business>) firebaseHandler.getUpdatedObject();
            addMarkers(businesses);
        });
    }

    void addMarkers(final List<Business> businesses) {
        Log.d(TAG, "addMarkers: Adding " + businesses.size() + " markers");
        for (Business business : businesses) {
            Log.d(TAG, "addMarkers: " + business);
            LatLng ll = new LatLng(business.getCoordinates().getLatitude(), business.getCoordinates().getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(ll).title(business.getName()));

            marker.setTag(business);
            MapMarkerAdapter adapter = new MapMarkerAdapter(this);
            mMap.setInfoWindowAdapter(adapter);
            mMap.setOnInfoWindowClickListener(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101 && permissions.length > 0) { // location permission request
            for (int idx = 0; idx < permissions.length; ++idx) {
                if (permissions[idx].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResults[idx] == PackageManager.PERMISSION_GRANTED) {
                        locationTracker.startTracking();
                        running = true;
                    } else {
                        shouldAsk = false;
                        SweetAlertDialog sDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Location service is needed!")
                                .setContentText("The app can not function without location permission")
                                .setConfirmText("Ok");
                        sDialog.setCanceledOnTouchOutside(true);
                        sDialog.show();
                    }
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationTracker.startTracking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTracker.stopTracking();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationTracker.stopTracking();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Log.d(TAG, "onInfoWindowClick: " + marker.getTitle());
    }
}