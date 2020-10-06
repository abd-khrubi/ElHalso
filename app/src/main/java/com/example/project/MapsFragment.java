package com.example.project;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.example.project.adapters.MapMarkerAdapter;
import com.example.project.data.Business;
import com.example.project.location.LocationInfo;
import com.example.project.location.LocationReceivedCallback;
import com.example.project.location.LocationTracker;
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

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "MapsFragment";
    private GoogleMap mMap;
    private LocationTracker locationTracker;

    private Context context;


    private View.OnClickListener buttonClick = view -> {
        Log.i(TAG, "tstButton: Click!!");
        if (!locationTracker.isTrackerReady()) {
            return;
        }
        final FirebaseHandler firebaseHandler = FirebaseHandler.getInstance();
        LocationInfo currentLocation = locationTracker.getLastLocation();
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
            addMarkers((ArrayList<Business>) firebaseHandler.getUpdatedObject());
        });
    };

    void addMarkers(final List<Business> businesses) {
        Log.d(TAG, "addMarkers: Adding " + businesses.size() + " markers");
        for (Business business : businesses) {
            Log.d(TAG, "addMarkers: " + business);
            LatLng ll = new LatLng(business.getCoordinates().getLatitude(), business.getCoordinates().getLongitude());
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(ll).title(business.getName()));

            marker.setTag(business);
            MapMarkerAdapter adapter = new MapMarkerAdapter(context);
            mMap.setInfoWindowAdapter(adapter);
            mMap.setOnInfoWindowClickListener(it -> {
                Log.i(TAG, "addMarkers: " + it.getTitle());
            });
        }
    }

    private LocationReceivedCallback onLocationUpdate = locationInfo -> {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(locationInfo.toLatLng()));
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        Button button = view.findViewById(R.id.tt);
        button.setOnClickListener(buttonClick);

        context = view.getContext();
        locationTracker = ((AppLoader) requireContext().getApplicationContext()).getLocationTracker();
        locationTracker.registerCallback(onLocationUpdate);
    }

    private CameraUpdate getZoomForDistance(LatLng originalPosition, double distance) {
        distance *= 1.5;
        LatLng rightBottom = SphericalUtil.computeOffset(originalPosition, distance, 135);
        LatLng leftTop = SphericalUtil.computeOffset(originalPosition, distance, -45);
        LatLngBounds sBounds = new LatLngBounds(new LatLng(rightBottom.latitude, leftTop.longitude), new LatLng(leftTop.latitude, rightBottom.longitude));
        return CameraUpdateFactory.newLatLngBounds(sBounds, 0);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onResume() {
        super.onResume();
        locationTracker.startTracking();
    }

    @Override
    public void onPause() {
        super.onPause();
        locationTracker.stopTracking();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        locationTracker.stopTracking();
    }
}