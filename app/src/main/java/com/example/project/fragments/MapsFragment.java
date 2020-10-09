package com.example.project.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;

import com.example.project.FirebaseHandler;
import com.example.project.MainMapActivity;
import com.example.project.R;
import com.example.project.adapters.MapMarkerAdapter;
import com.example.project.data.Business;
import com.example.project.location.LocationInfo;
import com.example.project.location.LocationReceivedCallback;
import com.example.project.location.LocationTracker;
import com.example.project.WazeAndBusinessPageActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
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

    private LocationReceivedCallback onLocationUpdate = locationInfo -> {
        if (mMap != null && isAdded()) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(locationInfo.toLatLng()));
            addMarkers(((MainMapActivity) requireActivity()).filterBusinesses());
        }
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
                Business b = (Business) it.getTag();
                Intent intent = new Intent(context, WazeAndBusinessPageActivity.class);
                intent.putExtra("business", b);
                startActivity(intent);
            });
        }
    }

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

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
//            ActivityCompat.requestPermissions(requireActivity(), permissions, 1010);
            requestPermissions(permissions, 1010);
            return;
        }

        locationTracker = ((MainMapActivity) requireActivity()).locationTracker;
        locationTracker.registerCallback(TAG, onLocationUpdate);
        ((MainMapActivity) requireActivity()).callbacks.put(TAG, () -> {
            if (locationTracker.getLastLocation() != null) {
                addMarkers(((MainMapActivity) requireActivity()).filterBusinesses());
            }
        });
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
        Context context = requireContext();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
//        mMap.setLocationSource(new MyLocationSource(((MainMapActivity)requireActivity()).locationTracker));
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationClickListener(location -> {
            Log.i(TAG, "onMapReady: " + location);
        });
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1010) {
            for (int i = 0; i < permissions.length; i++) {
                if ((permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) || permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION))
                        && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    SweetAlertDialog dialog = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE);
                    dialog.setTitleText("Location must be enabled");
                    dialog.setOnDismissListener(d -> {
                        d.dismiss();
                        String[] p = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                        requestPermissions(p, 1010);
                    });
                    dialog.show();
                    return;
                } else {
                    locationTracker = ((MainMapActivity) requireActivity()).locationTracker;
                    locationTracker.registerCallback(TAG, onLocationUpdate);
                }
            }
        }
    }

    private static class MyLocationSource implements LocationSource {

        private LocationTracker locationTracker;

        MyLocationSource(LocationTracker locationTracker) {

            this.locationTracker = locationTracker;
        }
        @Override
        public void activate(OnLocationChangedListener onLocationChangedListener) {
            locationTracker.registerCallback("loc_source", location -> {
                Location loc = new Location(LocationManager.GPS_PROVIDER);
                loc.setLatitude(location.getLatitude());
                loc.setLongitude(location.getLongitude());
                onLocationChangedListener.onLocationChanged(loc);
            });
        }

        @Override
        public void deactivate() {
            locationTracker.clearCallback("loc_source");
        }
    }
}