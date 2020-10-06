package com.example.project;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.adapters.CategoryItemRecyclerViewAdapter;
import com.example.project.callbacks.OnCategoryClick;
import com.example.project.data.Business;
import com.example.project.location.LocationTracker;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoriesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoriesFragment extends Fragment implements OnCategoryClick {

    private static final String TAG = "CategoriesFragment";

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;

    private CategoryItemRecyclerViewAdapter adapter;

    private List<Business> businesses = null;
    private Map<String, List<Business>> categories;

    public CategoriesFragment() {
        // Required empty public constructor
    }

    public static CategoriesFragment newInstance(int columnCount) {
        CategoriesFragment fragment = new CategoriesFragment();
        Bundle args = new Bundle();

        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            adapter = new CategoryItemRecyclerViewAdapter(this);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        // get businesses
        AppLoader appLoader = ((AppLoader) requireContext().getApplicationContext());
        LocationTracker locationTracker = appLoader.getLocationTracker();

        appLoader.fetchBusinesses(getViewLifecycleOwner(), locationTracker.getLastLocation().toGeoPoint(), 100000 /* user.radius */, () -> {
            businesses = appLoader.getBusinessList();
            getCategories();
            populateList();
        });
        return view;
    }

    private void getCategories() {
        categories = new HashMap<>();

        for (Business business : businesses) {
            if (categories.containsKey(business.getCategory())) {
                categories.get(business.getCategory()).add(business);
            } else {
                categories.put(business.getCategory(), Collections.singletonList(business));
            }
        }
    }

    public void populateList() {
        adapter.mValues.addAll(categories.keySet());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onCategoryClick(String category) {
        // Open Businesses list view
        Log.d(TAG, "onCategoryClick: " + category);
    }
}