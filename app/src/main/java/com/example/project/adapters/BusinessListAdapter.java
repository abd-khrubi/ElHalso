package com.example.project.adapters;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project.AppLoader;
import com.example.project.R;
import com.example.project.callbacks.OnBusinessClick;
import com.example.project.data.Business;
import com.example.project.data.User;
import com.example.project.location.LocationInfo;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.List;

public class BusinessListAdapter extends RecyclerView.Adapter<BusinessListAdapter.ViewHolder> {

    public List<Business> mValues;
    private OnBusinessClick onBusinessClick;


    private Context context;
    private LocationInfo locationInfo;

    public BusinessListAdapter(LocationInfo locationInfo, OnBusinessClick onBusinessClick) {
        this.mValues = new ArrayList<>();
        this.locationInfo = locationInfo;
        this.onBusinessClick = onBusinessClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.business_list_item, parent, false);

        final ViewHolder holder = new ViewHolder(view);

        view.setOnClickListener(v -> {
            int pos = holder.getLayoutPosition();
            onBusinessClick.onBusinessClick(mValues.get(pos));
        });

        this.context = context;

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Business item = mValues.get(position);
        User user = ((AppLoader) context.getApplicationContext()).getUser();

        boolean isFav = user.getFavorites().contains(item);
        holder.isFavImageView.setVisibility(isFav ? View.VISIBLE : View.INVISIBLE);
        holder.nameTextView.setText(item.getName());

        String distance = "";
        if (locationInfo != null && item.getCoordinates() != null) {
            float[] result = new float[1];
            LocationInfo loc1 = locationInfo;
            GeoPoint loc2 = item.getCoordinates();
            Location.distanceBetween(loc1.getLatitude(), loc1.getLongitude(), loc2.getLatitude(), loc2.getLongitude(), result);
            float dist = result[0];
            if (dist < 1000) {
                distance = String.format("%.2f m", dist);
            } else {
                distance = String.format("%.2f km", dist / 1000f);
            }
        }
        holder.distanceTextView.setText(distance);
        holder.setRating(item.getReviewsScore());
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private View view;

        private ImageView isFavImageView;
        private TextView nameTextView;
        private TextView distanceTextView;
        private LinearLayout ratingLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            view = itemView;
            isFavImageView = view.findViewById(R.id.business_is_fav_img);
            nameTextView = view.findViewById(R.id.business_item_name);
            distanceTextView = view.findViewById(R.id.business_distance);
            ratingLayout = view.findViewById(R.id.business_rating);
        }

        /**
         * @param rating between 0 and 1
         */
        public void setRating(float rating) {
            int stars_amt = (int) (rating * 2);
            for (int i = 0; i < stars_amt / 2; ++i) {
                ImageView star = new ImageView(view.getContext());
                star.setBackgroundResource(R.drawable.ic_twotone_star_24);
                ratingLayout.addView(star);
            }
            if (stars_amt % 2 == 1) {
                ImageView star = new ImageView(view.getContext());
                star.setBackgroundResource(R.drawable.ic_twotone_star_half_24);
                ratingLayout.addView(star);
            }
        }
    }
}
