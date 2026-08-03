package com.example.evapp.util;

import android.location.Location;

import com.example.evapp.model.Station;

import org.osmdroid.util.GeoPoint;

public class StationUtils {

    /**
     * Calculate distance from user to station in kilometers
     */
    public static double calculateDistance(double userLat, double userLon, double stationLat, double stationLon) {
        final double R = 6371.0; // Earth radius in km

        double dLat = Math.toRadians(stationLat - userLat);
        double dLon = Math.toRadians(stationLon - userLon);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(stationLat)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in km
    }

    /**
     * Calculate ETA in minutes
     */
    public static int calculateETA(double distanceKm, double avgSpeedKmh) {
        if (avgSpeedKmh <= 0) avgSpeedKmh = 40.0; // Default speed
        return (int) Math.round((distanceKm / avgSpeedKmh) * 60);
    }

    /**
     * Enrich station with distance and ETA from user location
     */
    public static void enrichStationWithLocation(Station station, double userLat, double userLon) {
        if (station == null) return;

        double distance = calculateDistance(userLat, userLon, station.latitude, station.longitude);
        int eta = calculateETA(distance, 40.0);  // average speed in km/h

        station.distance = (int) distance;
        station.eta = eta;
    }

    /**
     * Enrich station with distance and ETA from user location (Location object)
     */
    public static void enrichStationWithLocation(Station station, Location userLocation) {
        if (station == null || userLocation == null) return;
        enrichStationWithLocation(station, userLocation.getLatitude(), userLocation.getLongitude());
    }

    /**
     * Enrich station with distance and ETA from user location (GeoPoint)
     */
    public static void enrichStationWithLocation(Station station, GeoPoint userLocation) {
        if (station == null || userLocation == null) return;
        enrichStationWithLocation(station, userLocation.getLatitude(), userLocation.getLongitude());
    }

    /**
     * Format distance for display
     */
    public static String formatDistance(double km) {
        if (km < 1.0) {
            return String.format("%.0f m", km * 1000);
        }
        return String.format("%.1f km", km);
    }

    /**
     * Format ETA for display
     */
    public static String formatETA(int minutes) {
        if (minutes < 60) {
            return minutes + " min";
        }
        int hours = minutes / 60;
        int mins = minutes % 60;
        if (mins == 0) {
            return hours + "h";
        }
        return hours + "h " + mins + "m";
    }

}
