package com.example.evapp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Port {

    // Existing fields (from your current repo)
    public String name;
    public String status;           // "Free", "Occupied", etc.
    public double latitude;
    public double longitude;
    public String type;             // e.g., "CCS2", "Type2"
    public double power;            // kW (from JSON)
    public Double leaveInMinutes;   // optional
    public Double nextCarInMinutes; // optional

    // Existing: features list (already in your repo)
    public List<String> features;

    // ---------- NEW RUNTIME-ONLY FIELDS (NOT REQUIRED IN JSON) ----------

    // Distance from user in km (set at runtime by HomeActivity)
    public double distanceFromUserKm = -1;      // -1 = unknown / not set

    // Estimated arrival time in minutes (set at runtime by HomeActivity)
    public int estimatedArrivalMinutes = -1;    // -1 = unknown / not set

    // ---------- AVAILABILITY HELPERS (USING EXISTING FIELDS) ----------

    /**
     * Total number of ports at this station.
     * With current JSON, each entry represents one logical port/station,
     * so this returns 1 by default.
     */
    public int getTotalPortCount() {
        // If you later add per-port detail, you can change this.
        return 1;
    }

    /**
     * Number of available ports.
     * Uses the existing 'status' field.
     */
    public int getAvailablePortCount() {
        if (status == null) return 0;

        String s = status.toLowerCase(Locale.US);

        // Adjust these mappings based on how your JSON encodes status
        if (s.contains("free") || s.contains("available") || s.contains("vacant")) {
            return 1;
        }
        return 0;
    }

    /**
     * Number of occupied ports.
     */
    public int getOccupiedPortCount() {
        return getTotalPortCount() - getAvailablePortCount();
    }

    /**
     * Human-readable availability summary.
     * Examples:
     * - "Available"
     * - "Occupied"
     * - "Unknown"
     */
    public String getAvailabilitySummary() {
        if (status == null || status.trim().isEmpty()) return "Unknown";

        int total = getTotalPortCount();
        int available = getAvailablePortCount();

        if (total == 0) return "No port info";
        if (available == 0) return "Occupied";
        if (available == total) return "Available";
        return available + "/" + total + " available";
    }

    /**
     * Emoji representing overall availability.
     * 🟢 = available
     * 🔴 = occupied
     * 🟡 = partial/other
     * ❓ = unknown
     */
    public String getStatusEmoji() {
        if (status == null) return "❓";

        String s = status.toLowerCase(Locale.US);

        if (s.contains("free") || s.contains("available") || s.contains("vacant")) {
            return "🟢";
        }
        if (s.contains("occupied") || s.contains("busy") || s.contains("full")) {
            return "🔴";
        }
        if (s.contains("partial") || s.contains("limited")) {
            return "🟡";
        }
        return "❓";
    }

    // ---------- PORT SPEC HELPERS (BASED ON type + power) ----------

    /**
     * Short text like "1x CCS2 (50kW)" from existing fields.
     */
    public String getPortSpecText() {
        String t = (type == null || type.trim().isEmpty()) ? "-" : type;
        String p = (power > 0) ? (String.format(Locale.US, "%.0f", power) + "kW") : "-";
        return "1x " + t + " (" + p + ")";
    }

    // ---------- DISTANCE / ETA HELPERS (SET BY HomeActivity) ----------

    /**
     * Distance text for UI.
     * Examples:
     * - "2.3 km away"
     * - "450 m away"
     * - "" (if unknown)
     */
    public String getDistanceText() {
        if (distanceFromUserKm < 0) return "";

        if (distanceFromUserKm < 1.0) {
            // Show meters if less than 1 km
            return String.format(Locale.US, "%.0f m away", distanceFromUserKm * 1000.0);
        }
        return String.format(Locale.US, "%.1f km away", distanceFromUserKm);
    }

    /**
     * ETA text for UI, using value set in HomeActivity.
     * Examples:
     * - "~12 min"
     * - "~1h 05min"
     * - "" (if unknown)
     */
    public String getETAText() {
        if (estimatedArrivalMinutes < 0) return "";

        if (estimatedArrivalMinutes < 60) {
            return "~" + estimatedArrivalMinutes + " min";
        }

        int hours = estimatedArrivalMinutes / 60;
        int mins = estimatedArrivalMinutes % 60;
        return String.format(Locale.US, "~%dh %02dmin", hours, mins);
    }

    // ---------- FEATURES / AMENITIES HELPERS ----------

    /**
     * Returns a short list (up to 3) of features for compact display.
     */
    public List<String> getTopFeatures(int maxCount) {
        List<String> out = new ArrayList<>();
        if (features == null || features.isEmpty() || maxCount <= 0) return out;

        int n = Math.min(maxCount, features.size());
        for (int i = 0; i < n; i++) {
            out.add(features.get(i));
        }
        return out;
    }

    /**
     * Returns comma-separated features, or "No amenities listed" if empty.
     */
    public String getFeaturesText() {
        if (features == null || features.isEmpty()) {
            return "No amenities listed";
        }
        return String.join(", ", features);
    }

    // ---------- VALIDATION HELPERS ----------

    /**
     * Check if this Port has valid GPS coordinates.
     */
    public boolean hasValidLocation() {
        // Reject (0, 0) - Gulf of Guinea
        if (latitude == 0.0 && longitude == 0.0) return false;

        // Reject invalid lat/lon ranges
        if (latitude < -90 || latitude > 90) return false;
        if (longitude < -180 || longitude > 180) return false;

        return true;
    }
}
