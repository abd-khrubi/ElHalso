package com.example.project.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.project.AppLoader;
import com.example.project.BusinessActivity;
import com.example.project.MainMapActivity;
import com.example.project.R;
import com.example.project.adapters.MapMarkerAdapter;
import com.example.project.data.Business;
import com.example.project.data.User;
import com.example.project.location.LocationReceivedCallback;
import com.example.project.location.LocationTracker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "MapsFragment";
    private GoogleMap mMap;
    private LocationTracker locationTracker;
    private Circle radiusCircle;
    private Context context;
    private boolean followLocation = true;

    private LocationReceivedCallback onLocationUpdate = locationInfo -> {
        if (mMap != null && isAdded()) {
            addMarkers(((MainMapActivity) requireActivity()).filterBusinesses());
            User user = ((AppLoader) requireContext().getApplicationContext()).getUser();
            radiusCircle.setVisible(true);
            radiusCircle.setCenter(locationInfo.toLatLng());
            radiusCircle.setRadius(user.getRadius() * 1000);
            if (followLocation) {
                mMap.animateCamera(getZoomForDistance(locationTracker.getLastLocation().toLatLng(), user.getRadius() * 1000));
            }
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
                Intent intent = new Intent(context, BusinessActivity.class);
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

        context = view.getContext();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
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
        mMap.setMyLocationEnabled(true);
        radiusCircle = mMap.addCircle(new CircleOptions()
                .visible(false)
                .center(new LatLng(0, 0))
                .fillColor(getContext().getColor(R.color.user_radius_color))
                .strokeWidth(0.1f)
        );

        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                followLocation = false;
            }
        });
        mMap.setOnMyLocationButtonClickListener(() -> {
            if (locationTracker.getLastLocation() != null) {
                User user = ((AppLoader) requireContext().getApplicationContext()).getUser();
                mMap.animateCamera(getZoomForDistance(locationTracker.getLastLocation().toLatLng(), user.getRadius() * 1000));
                followLocation = true;
                return true;
            }
            return false;
        });
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
}