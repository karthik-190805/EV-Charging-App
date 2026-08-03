package com.example.evapp.model;

import com.google.gson.annotations.SerializedName;

public class ChargerPort {

    @SerializedName("portId")
    public String portId;

    @SerializedName("connectorType")
    public String connectorType;

    @SerializedName("power")
    public String power;  // e.g., "50kW", "22kW"

    @SerializedName("status")
    public String status;  // "Available", "Occupied", "Under Maintenance"

    @SerializedName("currentSession")
    public ChargingSession currentSession;

    // Constructor
    public ChargerPort() {
        this.status = "Available";
    }

    public boolean isAvailable() {
        return "Available".equalsIgnoreCase(status);
    }

    public boolean isOccupied() {
        return "Occupied".equalsIgnoreCase(status);
    }

    public double getPowerKW() {
        if (power == null || power.isEmpty()) return 0;
        try {
            // Extract number from "50kW" or "22kW"
            return Double.parseDouble(power.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
