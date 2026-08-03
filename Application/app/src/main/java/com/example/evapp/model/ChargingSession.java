package com.example.evapp.model;

import com.google.gson.annotations.SerializedName;

public class ChargingSession {

    @SerializedName("startTime")
    public String startTime;           // ISO 8601 format

    @SerializedName("estimatedEndTime")
    public String estimatedEndTime;    // ISO 8601 format

    @SerializedName("vehicleType")
    public String vehicleType;         // e.g., "Tesla Model 3"

    // Constructor
    public ChargingSession() {
    }
}
