package com.example.project;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.adapters.MainRecAdapter;
import com.example.project.data.Section;

import java.util.List;

public class SpecificBusinessListActivity extends AppCompatActivity {
    List<Section> sectionList;
    RecyclerView mainRecycler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_business_list);
        initData();
        mainRecycler = findViewById(R.id.mainRec);
//        mainRecyclerView = findViewById(R.id.mainRecyclerView);
        MainRecAdapter mainRecyclerAdapter = new MainRecAdapter(sectionList);
        mainRecycler.setAdapter(mainRecyclerAdapter);
        mainRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));


    }
    private void initData(){
        //creates a section from favorites and a section of businesses
        //to initiate use
        return;
    }
}