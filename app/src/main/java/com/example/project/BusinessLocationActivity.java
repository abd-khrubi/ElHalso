package com.example.project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.example.project.data.Business;
import com.example.project.location.LocationInfo;
import com.example.project.location.LocationReceivedCallback;
import com.example.project.location.LocationTracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.GeoPoint;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

public class BusinessLocationActivity extends AppCompatActivity implements OnMapReadyCallback, LocationReceivedCallback {

    private static final String TAG = "BusinessLocationActivit";
    public static final int PERMISSION_REQ = 1234;

    private GoogleMap mMap;
    private Marker currentChoice;
    private LocationTracker locationTracker;
    private Business business;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_location);

        business = ((AppLoader)getApplicationContext()).getBusiness();
        setSupportActionBar((Toolbar) findViewById(R.id.businessLocationToolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(business.getName() != null ? business.getName() : "<No Name>");
        getSupportActionBar().setSubtitle("Set Location");

        locationTracker = new LocationTracker(this);
        locationTracker.registerCallback(TAG, this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMapClickListener(latLng -> {
            if (currentChoice == null) {
                currentChoice = mMap.addMarker(new MarkerOptions().position(latLng));
            } else {
                currentChoice.setPosition(latLng);
            }
            findViewById(R.id.confirmLocationBtn).setVisibility(View.VISIBLE);
        });
        if(business.getCoordinates() != null) {
            LatLng pos = new LatLng(business.getCoordinates().getLatitude(), business.getCoordinates().getLongitude());
            currentChoice = mMap.addMarker(new MarkerOptions().position(pos));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 13));
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQ);
            return;
        }

        mMap.setMyLocationEnabled(true);
    }

    /**
     * SetLocationManually button event
     */
    public void onSetLocationManuallyButton(View view) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        final View promptView = layoutInflater.inflate(R.layout.dialog_set_location_manually, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);
        final AlertDialog alertD = alertDialogBuilder.create();

        promptView.findViewById(R.id.viewBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String latitude = ((EditText)promptView.findViewById(R.id.latitudeTxt)).getText().toString();
                String longitude = ((EditText)promptView.findViewById(R.id.longitudeTxt)).getText().toString();

                if(latitude.equals("") || longitude.equals("")){
                    Toast.makeText(BusinessLocationActivity.this, "Can't enter empty value!", Toast.LENGTH_SHORT).show();
                    return;
                }
                LatLng pos = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
                if (currentChoice == null) {
                    currentChoice = mMap.addMarker(new MarkerOptions().position(pos));
                } else {
                    currentChoice.setPosition(pos);
                }
                findViewById(R.id.confirmLocationBtn).setVisibility(View.VISIBLE);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 13));
                alertD.dismiss();
            }
        });

        alertD.show();
    }

    /**
     * Home button event
     */
    public void onSetButton(View view) {
        business.setCoordinates(new GeoPoint(currentChoice.getPosition().latitude, currentChoice.getPosition().longitude));
        FirebaseHandler.getInstance().updateBusinessLocation(business);
        finish();
    }

    /**
     * Location received from location tracker
     *
     * @param location
     */
    @Override
    public void onLocationReceived(LocationInfo location) {
        if (currentChoice == null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    location.toLatLng(),
                    13
            ));
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ) {
            for (int i = 0; i < permissions.length; i++) {
                if ((permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION) || permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION))
                        && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    locationTracker.registerCallback(TAG, this);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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