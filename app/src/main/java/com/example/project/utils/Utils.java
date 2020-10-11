package com.example.project.utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Utils {
    public static float distanceBetween(LatLng pos1, LatLng pos2) {
        float[] res = new float[1];
        Location.distanceBetween(pos1.latitude, pos1.longitude, pos2.latitude, pos2.longitude, res);
        return res[0];
    }
}
