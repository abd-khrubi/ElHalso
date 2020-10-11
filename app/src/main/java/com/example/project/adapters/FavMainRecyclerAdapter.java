package com.example.project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.data.Business;
import com.example.project.data.FavSection;

import java.util.List;

public class FavMainRecyclerAdapter extends RecyclerView.Adapter<FavMainRecyclerAdapter.ViewHolder> {

    List<FavSection> sectionList;
    Context context;
    public FavMainRecyclerAdapter(List<FavSection> sectionList, Context context) {
        this.sectionList = sectionList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.section_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        FavSection section = sectionList.get(position);
        String sectionName = section.getSectionName();
        List<Business> items = section.getSectionItems();

        holder.sectionNameTextView.setText(sectionName);

        FavChildRecycleAdapter childRecyclerAdapter = new FavChildRecycleAdapter(items, this.context);
        holder.childRecyclerView.setAdapter(childRecyclerAdapter);

    }

    @Override
    public int getItemCount() {
        return sectionList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView sectionNameTextView;
        RecyclerView childRecyclerView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            sectionNameTextView = itemView.findViewById(R.id.sectionNameTextView);
            childRecyclerView = itemView.findViewById(R.id.childRecyclerView);
        }
    }
}