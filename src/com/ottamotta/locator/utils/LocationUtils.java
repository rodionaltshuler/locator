package com.ottamotta.locator.utils;


import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class LocationUtils {

    static String directions[] = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};

    public static String getDirection(float azimuth) {
        int index = (int) Math.abs((((azimuth - 45 / 2) % 360) / 45));
        return directions[index + 1];
    }

    public static Location createLocation(double lat, double lon) {
        Location loc = new Location("");
        loc.setLatitude(lat);
        loc.setLongitude(lon);
        return loc;
    }

    public static LatLng parseLocation(String src) {
        try {
            String[] coords = src.split(",");
            double lat = Double.valueOf(coords[0]);
            double lon = Double.valueOf(coords[1]);
            return new LatLng(lat, lon);
        } catch (Exception e) {
            return null;
        }
    }

    private static final NumberFormat locationFormat = new DecimalFormat("#.######");

    public static String getLocationFormatted(LatLng loc) {
        return locationFormat.format(loc.latitude) + ", " + locationFormat.format(loc.longitude);
    }
    public static String getLocationFormattedNoWhitespace(LatLng loc) {
        return locationFormat.format(loc.latitude) + "," + locationFormat.format(loc.longitude);
    }

}
