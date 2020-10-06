package com.example.project.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.project.R;
import com.example.project.data.Business;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class MapMarkerAdapter implements GoogleMap.InfoWindowAdapter {
    private static final String TAG = "MapMarkerAdapter";

    private Context context;

    public MapMarkerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        // not implemented
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = ((Activity) context).getLayoutInflater().inflate(R.layout.marker_info, null);

        TextView title = view.findViewById(R.id.marker_name);
        TextView desc = view.findViewById(R.id.marker_desc);
        LinearLayout starsLayout = view.findViewById(R.id.stars_container);


        Business business = (Business) marker.getTag();
        if (business == null) {
            Log.w(TAG, "getInfoContents: Invalid marker");
            title.setText(marker.getTitle());
            desc.setVisibility(View.INVISIBLE);
            starsLayout.setVisibility(View.INVISIBLE);
            return view;
        }

        title.setText(business.getName());
        desc.setText(business.getDescription());

        int starsAmt = (int) (business.getReviewsScore() * 10);

        if (starsAmt == 0) {
            starsLayout.setVisibility(View.INVISIBLE);
        } else {
            for (int i = 0; i < (int) (starsAmt / 2); ++i) {
                ImageView ii = new ImageView(context);
                ii.setBackgroundResource(R.drawable.ic_twotone_star_24);
                starsLayout.addView(ii);
            }

            if (starsAmt % 2 == 1) {
                ImageView ii = new ImageView(context);
                ii.setBackgroundResource(R.drawable.ic_twotone_star_half_24);
                starsLayout.addView(ii);
            }
        }

        return view;
    }
}
