package com.example.project.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.R;
import com.example.project.data.BusinessRow;

import java.util.List;

public class ChildRecyclerAdapter extends RecyclerView.Adapter<ChildRecyclerAdapter.ChildViewHolder> {

    List<BusinessRow> items;

    public ChildRecyclerAdapter(List<BusinessRow> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_row, parent, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
        BusinessRow b_r = items.get(position);
        String businessName = b_r.getBusiness_name();
        float rating = b_r.getRating();
        float distance = b_r.getDistance();
        holder.tv_businessName.setText(businessName);
        String t = Float.toString(distance);
        holder.tv_distance.setText(t);
        holder.tv_rating.setText(Float.toString(rating));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ChildViewHolder extends RecyclerView.ViewHolder {

        TextView tv_businessName;
        TextView tv_rating;
        TextView tv_distance;

        public ChildViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_businessName = itemView.findViewById(R.id.business_name);
            tv_rating = itemView.findViewById(R.id.rating);
            tv_distance = itemView.findViewById(R.id.distance);
        }
    }
}