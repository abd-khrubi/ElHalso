package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.adapters.BusinessListAdapter;
import com.example.project.callbacks.OnBusinessClick;
import com.example.project.data.Business;
import com.example.project.data.User;
import com.example.project.location.LocationInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BusinessListActivity extends AppCompatActivity implements OnBusinessClick {

    private static final String TAG = "BusinessListActivity";

    private BusinessListAdapter adapter;
    private RecyclerView recyclerView;
    private List<Business> businesses;
    private String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_list);

        Gson gson = new Gson();
        String payload = getIntent().getStringExtra("business_list");
        if (payload == null || payload.length() == 0) {
            payload = "[]";
        }

        Type listType = new TypeToken<ArrayList<Business>>() {
        }.getType();
        businesses = gson.fromJson(payload, listType);
        payload = getIntent().getStringExtra("user_location");
        LocationInfo locationInfo = null;
        if (payload != null && payload.length() > 0) {
            locationInfo = gson.fromJson(payload, LocationInfo.class);
        }

        category = getIntent().getStringExtra("cat_name");

        setSupportActionBar((Toolbar) findViewById(R.id.user_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(category);

        recyclerView = findViewById(R.id.business_list_recycler);
        adapter = new BusinessListAdapter(locationInfo, this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        populateList();
    }

    private void populateList() {
        User user = ((AppLoader) getApplicationContext()).getUser();

        for (Business business : businesses) {
            if (user.getFavorites().contains(business)) {
                adapter.mValues.add(business);
            }
        }
        for (Business business : businesses) {
            if (!user.getFavorites().contains(business)) {
                adapter.mValues.add(business);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                ((AppLoader) getApplicationContext()).openProfile(this, businesses);
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBusinessClick(Business business) {
        Intent intent = new Intent(this, WazeAndBusinessPageActivity.class);
        intent.putExtra("business", business);
        startActivity(intent);
    }
}