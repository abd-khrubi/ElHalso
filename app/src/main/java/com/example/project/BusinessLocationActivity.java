package com.example.project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ontbee.legacyforks.cn.pedant.SweetAlert.SweetAlertDialog;

public class BusinessLocationActivity extends AppCompatActivity implements OnMapReadyCallback, LocationReceivedCallback {

    private static final String TAG = "BusinessLocationActivit";
    public static final int PERMISSION_REQ = 1234;

    private GoogleMap mMap;
    private Marker currentChoice;
    private LocationTracker locationTracker;
    private SweetAlertDialog loadingDialog;

    private Business business;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_location);

        if (getIntent().hasExtra("business")) {
            business = getIntent().getParcelableExtra("business");
        }

        setSupportActionBar((Toolbar) findViewById(R.id.businessLocationToolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(business != null ? business.getName() :
                getResources().getString(R.string.app_name)
        );

        locationTracker = new LocationTracker(this);
        locationTracker.registerCallback(TAG, this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        loadingDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        loadingDialog.setTitleText("Loading location")
                .setCanceledOnTouchOutside(false);
        loadingDialog.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_REQ);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMapClickListener(latLng -> {
            if (currentChoice == null) {
                currentChoice = mMap.addMarker(new MarkerOptions().position(latLng));
            } else {
                currentChoice.setPosition(latLng);
            }
        });
    }

    /**
     * SetLocationManually button event
     */
    public void onSetLocationButton(View view) {
        // TODO enter location manually
    }

    /**
     * Home button event
     */
    public void onHomeButton(View view) {
        Intent intent = new Intent();
        if (currentChoice != null) {
            intent.putExtra("location", currentChoice.getPosition());
        }
        setResult(RESULT_OK, intent); // Maybe return `RESULT_CANCELED` if no location is set?
        finish();
    }

    /**
     * Location received from location tracker
     *
     * @param location
     */
    @Override
    public void onLocationReceived(LocationInfo location) {
        if (loadingDialog != null) {
            loadingDialog.dismissWithAnimation();
            loadingDialog = null;
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
                        && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
                    dialog.setTitleText("Location must be enabled");
                    dialog.setOnDismissListener(d -> {
                        d.dismiss();
                        String[] p = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                        requestPermissions(p, PERMISSION_REQ);
                    });
                    dialog.show();
                    return;
                } else {
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