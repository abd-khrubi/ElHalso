package com.example.project;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.adapters.FavMainRecyclerAdapter;
import com.example.project.data.Business;
import com.example.project.data.FavSection;
import com.example.project.data.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ProfileMenuActivity extends AppCompatActivity {
    List<FavSection> sectionList = new ArrayList<>();
    RecyclerView mainRecyclerView;

    private List<Business> businesses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile__menu);

        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Business>>() {
        }.getType();
        businesses = gson.fromJson(getIntent().getStringExtra("businesses"), listType);

        initData();

        mainRecyclerView = findViewById(R.id.mainRecyclerView);
        FavMainRecyclerAdapter mainRecyclerAdapter = new FavMainRecyclerAdapter(sectionList, this);
        mainRecyclerView.setAdapter(mainRecyclerAdapter);
        mainRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    }

    private void initData() {
        AppLoader context = (AppLoader) getApplicationContext();
        String[] stArr = context.getResources().getStringArray(R.array.categories);
        for (String s : stArr) {
            for (Business b : getFavorites(context.getUser())) {
                List<Business> bus = new ArrayList<>();
                if (b.getCategory().equals(s)) {
                    bus.add(b);
                }
                sectionList.add(new FavSection(s, bus));
            }
        }
    }
    private List<Business> getFavorites(User user) {
        List<Business> favs = new ArrayList<>();
        for (Business b : businesses) {
            if (user.getFavorites().contains(b.getId())) {
                favs.add(b);
            }
        }
        return favs;
    }
}