package com.example.evapp.model;

import com.google.gson.annotations.SerializedName;

public class PricingInfo {

    @SerializedName("baseRate")
    public double baseRate;          // ₹/kWh during normal hours

    @SerializedName("peakRate")
    public Double peakRate;          // ₹/kWh during peak hours (nullable)

    @SerializedName("peakHours")
    public String peakHours;         // e.g., "18:00-22:00"

    @SerializedName("idleFee")
    public double idleFee;           // ₹/minute after charging complete

    @SerializedName("sessionFee")
    public double sessionFee;        // Fixed ₹ per session

    // Constructor
    public PricingInfo() {
        this.baseRate = 10.0;
        this.idleFee = 1.0;
        this.sessionFee = 5.0;
    }

    // ⚡ REQUIRED METHOD - Used by StationInfoWindow
    public String getFormattedRate() {
        return "₹" + String.format("%.1f", baseRate) + "/kWh";
    }

    // Display full pricing breakdown
    public String getFullPricingDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("Base: ₹").append(String.format("%.1f", baseRate)).append("/kWh");

        if (peakRate != null && peakRate > 0) {
            sb.append("\nPeak: ₹").append(String.format("%.1f", peakRate)).append("/kWh");
            if (peakHours != null && !peakHours.isEmpty()) {
                sb.append(" (").append(peakHours).append(")");
            }
        }

        if (idleFee > 0) {
            sb.append("\nIdle: ₹").append(String.format("%.1f", idleFee)).append("/min");
        }

        if (sessionFee > 0) {
            sb.append("\nSession: ₹").append(String.format("%.0f", sessionFee));
        }

        return sb.toString();
    }

    // Calculate total cost
    public double getTotalCost(double energyKWh, int idleMinutes) {
        double energyCost = energyKWh * baseRate;
        double idleCost = idleMinutes * idleFee;
        return energyCost + idleCost + sessionFee;
    }

    // Get current applicable rate (base or peak)
    public double getCurrentRate() {
        // TODO: Check if current time is in peak hours
        // For now, return base rate
        return baseRate;
    }

    // Check if peak rate applies
    public boolean isPeakHoursNow() {
        // TODO: Implement time check against peakHours
        return false;
    }
}
