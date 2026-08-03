package com.example.evapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Station {

    // Basic info
    public String name;

    @SerializedName("address")
    public String address;

    public double latitude;
    public double longitude;

    // Charging info
    public String type;
    public double power;
    public String status;

    // New enhanced fields
    @SerializedName("features")
    public List<String> features;

    @SerializedName("operatingHours")
    public String operatingHours;

    @SerializedName("ports")
    public List<ChargerPort> ports;

    @SerializedName("pricing")
    public PricingInfo pricing;

    // Optional fields (not in JSON)
    public transient Integer distance;
    public transient Integer eta;
    public transient Integer leaveInMinutes;
    public transient Integer nextCarInMinutes;

    // Constructor
    public Station() {
        this.features = new ArrayList<>();
        this.ports = new ArrayList<>();
        this.operatingHours = "24/7";
    }

    // ========== VALIDATION ==========

    public boolean hasValidLocation() {
        return latitude != 0.0 && longitude != 0.0;
    }

    public boolean hasAvailablePort() {
        if (ports == null || ports.isEmpty()) {
            // Fallback to old status field
            return "Free".equalsIgnoreCase(status) || "Available".equalsIgnoreCase(status);
        }

        for (ChargerPort port : ports) {
            if ("Available".equalsIgnoreCase(port.status)) {
                return true;
            }
        }
        return false;
    }

    // ========== PORT COUNTS ==========

    public int getTotalPortsCount() {
        if (ports == null || ports.isEmpty()) {
            return 1; // Assume 1 port if not specified
        }
        return ports.size();
    }

    public int getAvailablePortsCount() {
        if (ports == null || ports.isEmpty()) {
            return hasAvailablePort() ? 1 : 0;
        }

        int count = 0;
        for (ChargerPort port : ports) {
            if ("Available".equalsIgnoreCase(port.status)) {
                count++;
            }
        }
        return count;
    }

    public int getOccupiedPortsCount() {
        return getTotalPortsCount() - getAvailablePortsCount();
    }

    // ========== DISPLAY METHODS ==========

    public String getAvailabilitySummary() {
        int available = getAvailablePortsCount();
        int total = getTotalPortsCount();

        if (available == total) return "Available";
        if (available == 0) return "Occupied";
        return available + "/" + total + " available";
    }

    public String getPortSummary() {
        if (ports == null || ports.isEmpty()) {
            // Fallback to old format
            String portType = (type != null && !type.isEmpty()) ? type : "Type2";
            return "1x " + portType + " (" + (int)power + "kW)";
        }

        // Group by connector type
        java.util.Map<String, Integer> availableByType = new java.util.HashMap<>();
        java.util.Map<String, Integer> totalByType = new java.util.HashMap<>();

        for (ChargerPort port : ports) {
            String connectorType = port.connectorType;
            totalByType.put(connectorType, totalByType.getOrDefault(connectorType, 0) + 1);

            if ("Available".equalsIgnoreCase(port.status)) {
                availableByType.put(connectorType, availableByType.getOrDefault(connectorType, 0) + 1);
            }
        }

        // Build summary string
        StringBuilder sb = new StringBuilder();
        for (String connectorType : totalByType.keySet()) {
            if (sb.length() > 0) sb.append(", ");

            int available = availableByType.getOrDefault(connectorType, 0);
            int total = totalByType.get(connectorType);

            sb.append(available).append("/").append(total).append(" ").append(connectorType);
        }

        return sb.toString();
    }

    public String getStatusEmoji() {
        if (hasAvailablePort()) return "🟢";
        return "🔴";
    }

    // ========== DISTANCE & ETA ==========

    public String getDistanceDisplay() {
        if (distance == null) return "";
        if (distance < 1000) return distance + " m";
        return String.format("%.1f km", distance / 1000.0);
    }

    public void setDistance(int meters) {
        this.distance = meters;
    }

    public void setETA(int minutes) {
        this.eta = minutes;
    }

    public String getDistanceText() {
        if (distance == null) return "";
        return getDistanceDisplay();
    }

    public String getETAText() {
        if (eta == null) return "";
        return eta + " min";
    }

    // ========== PRICING ==========

    public String getPricingDisplay() {
        if (pricing == null) return "Pricing not available";
        return "₹" + pricing.baseRate + "/kWh";
    }

    public double getCurrentRate() {
        if (pricing == null) return 10.0; // Default rate

        // Check if currently in peak hours (simplified - always return base rate for now)
        return pricing.baseRate;
    }

    // ========== DISTANCE & ETA CALCULATION ==========

    /**
     * Calculate and set distance from given location
     */
    public void calculateDistance(double userLat, double userLon) {
        if (!hasValidLocation()) return;

        float[] results = new float[1];
        android.location.Location.distanceBetween(
                userLat, userLon,
                this.latitude, this.longitude,
                results
        );
        this.distance = Math.round(results[0]); // meters
        this.eta = calculateETAFromDistance(this.distance);
    }

    /**
     * Calculate ETA based on distance
     */
    private int calculateETAFromDistance(int distanceMeters) {
        if (distanceMeters <= 0) return 0;
        double distanceKm = distanceMeters / 1000.0;
        double avgSpeedKmh = 30.0; // City traffic average
        double timeHours = distanceKm / avgSpeedKmh;
        return (int) Math.ceil(timeHours * 60); // minutes
    }

}
