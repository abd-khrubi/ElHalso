package com.example.project;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.project.callbacks.BusinessListReadyCallback;
import com.example.project.callbacks.OnBusinessesReady;
import com.example.project.data.Business;
import com.example.project.data.User;
import com.example.project.location.LocationInfo;
import com.example.project.location.LocationTracker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainMapActivity extends AppCompatActivity implements BusinessListReadyCallback {

    private static final String TAG = "MainMapActivity";

    public LocationTracker locationTracker;

    private Menu menu;

    public Map<String, OnBusinessesReady> callbacks; // callbacks for when businesses list changed
    public List<Business> businessList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_map);

        setSupportActionBar((Toolbar) findViewById(R.id.user_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle(getResources().getString(R.string.app_name));

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_map, R.id.navigation_list
        ).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        locationTracker = new LocationTracker(this);

        callbacks = new HashMap<>();
        FirebaseHandler.getInstance().businessListener(this);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_screen_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                ((AppLoader) getApplicationContext()).logout(this);
                break;
            case R.id.action_settings:
                // TODO open settings
                Log.i(TAG, "onOptionsItemSelected: Settings");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
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

    public List<Business> filterBusinesses() {
        List<Business> filtered = new ArrayList<>();
        User user = ((AppLoader) getApplicationContext()).getUser();
        LocationInfo location = locationTracker.getLastLocation();
        for (Business business : businessList) {
            GeoPoint bLoc = business.getCoordinates();
            if (bLoc == null) {
                continue;
            }
            float[] res = new float[1];
            Location.distanceBetween(bLoc.getLatitude(), bLoc.getLongitude(), location.getLatitude(), location.getLongitude(), res);
            if (res[0] < user.getRadius() * 1000) {
                businessList.add(business);
            }
        }
        return filtered;
    }

    @Override
    public void onBusinessListReady(List<Business> businessList) {
        this.businessList = businessList;
        for (OnBusinessesReady callback : callbacks.values()) {
            callback.call();
        }
    }
}