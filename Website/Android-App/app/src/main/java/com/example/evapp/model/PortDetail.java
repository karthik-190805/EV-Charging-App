package com.example.evapp.model;

public class PortDetail {
    public int portNumber;          // Port 1, Port 2, etc.
    public String type;             // "CCS", "Type2", "CHAdeMO"
    public double powerKw;          // 50.0, 22.0, 7.4, etc.
    public String status;           // "Available", "Occupied", "Out of Service"
    public String currentUserId;    // User ID if occupied (null if available)
    public Long estimatedFreeAt;    // Unix timestamp (milliseconds) when port becomes free
    public String connector;        // "CCS Combo 2", "Type 2 Socket", etc.

    // Default constructor
    public PortDetail() {}

    // Constructor with basic params
    public PortDetail(int portNumber, String type, double powerKw, String status) {
        this.portNumber = portNumber;
        this.type = type;
        this.powerKw = powerKw;
        this.status = status;
    }

    /**
     * Get display name for this port
     * Example: "Port 1 - CCS (50kW)"
     */
    public String getDisplayName() {
        return "Port " + portNumber + " - " + type + " (" + powerKw + "kW)";
    }

    /**
     * Check if port is available for charging
     */
    public boolean isAvailable() {
        return "Available".equalsIgnoreCase(status);
    }

    /**
     * Check if port is occupied
     */
    public boolean isOccupied() {
        return "Occupied".equalsIgnoreCase(status);
    }

    /**
     * Check if port is out of service
     */
    public boolean isOutOfService() {
        return "Out of Service".equalsIgnoreCase(status) ||
                "Maintenance".equalsIgnoreCase(status);
    }

    /**
     * Get time remaining until port is free (in minutes)
     * Returns -1 if port is available or no estimate available
     */
    public int getMinutesUntilFree() {
        if (estimatedFreeAt == null || !isOccupied()) {
            return -1;
        }

        long now = System.currentTimeMillis();
        long diff = estimatedFreeAt - now;

        if (diff <= 0) return 0;

        return (int) (diff / 60000);  // Convert ms to minutes
    }

    /**
     * Get status emoji
     */
    public String getStatusEmoji() {
        if (isAvailable()) return "✅";
        if (isOccupied()) return "🔴";
        if (isOutOfService()) return "⚠️";
        return "❓";
    }

    /**
     * Get full status text with emoji
     * Example: "✅ Available", "🔴 Occupied (Free in 15 min)"
     */
    public String getFullStatusText() {
        String emoji = getStatusEmoji();
        String statusText = status;

        if (isOccupied() && getMinutesUntilFree() > 0) {
            statusText += " (Free in " + getMinutesUntilFree() + " min)";
        }

        return emoji + " " + statusText;
    }
}
