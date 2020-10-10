package com.example.project.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.AppLoader;
import com.example.project.FirebaseHandler;
import com.example.project.R;
import com.example.project.data.Business;

import java.util.List;

public class FavChildRecycleAdapter extends RecyclerView.Adapter<FavChildRecycleAdapter.ViewHolder> {

    List<Business> items;
    Context context;

    public FavChildRecycleAdapter(List<Business> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        this.context = parent.getContext();
        View view = layoutInflater.inflate(R.layout.fav_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemTextView.setText(items.get(position).getName());
        AppLoader context = (AppLoader) this.context.getApplicationContext();
        holder.delBtn.setOnClickListener(v -> {
            Business to_be_deleted = items.get(position);
            items.remove(position);
            context.getUser().removeFavoriteBusiness(to_be_deleted);
            notifyDataSetChanged();
            final FirebaseHandler firebaseHandler = FirebaseHandler.getInstance();
            firebaseHandler.removeFavoriteBusiness(context.getUser(), to_be_deleted);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView itemTextView;
        ImageView delBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            itemTextView = itemView.findViewById(R.id.fav_row_tv);
            delBtn = itemView.findViewById(R.id.favDeleteBtn);

        }
    }
}
