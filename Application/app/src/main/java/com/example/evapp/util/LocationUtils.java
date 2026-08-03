package com.example.evapp.util;

import android.location.Location;

public class LocationUtils {

    /**
     * Calculate distance between two coordinates in meters
     * Uses Haversine formula for accuracy
     */
    public static int calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return Math.round(results[0]); // Returns distance in meters
    }

    /**
     * Calculate ETA in minutes based on distance and average speed
     * Assumes 30 km/h average speed in city traffic
     */
    public static int calculateETA(int distanceInMeters) {
        if (distanceInMeters <= 0) return 0;

        double distanceInKm = distanceInMeters / 1000.0;
        double avgSpeedKmh = 30.0; // Average city driving speed
        double timeInHours = distanceInKm / avgSpeedKmh;
        int timeInMinutes = (int) Math.ceil(timeInHours * 60);

        return timeInMinutes;
    }

    /**
     * Format distance for display
     */
    public static String formatDistance(int meters) {
        if (meters < 1000) {
            return meters + " m";
        } else {
            double km = meters / 1000.0;
            return String.format("%.1f km", km);
        }
    }

    /**
     * Format ETA for display
     */
    public static String formatETA(int minutes) {
        if (minutes <= 0) return "Now";
        if (minutes < 60) return minutes + " min";

        int hours = minutes / 60;
        int mins = minutes % 60;
        if (mins == 0) return hours + " hr";
        return hours + " hr " + mins + " min";
    }

    /**
     * Check if location is valid
     */
    public static boolean isValidLocation(double lat, double lon) {
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180
                && lat != 0.0 && lon != 0.0;
    }
}
