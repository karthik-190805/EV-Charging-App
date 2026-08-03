package com.example.evapp.model;

public class Car {
    // Fields from JSON
    public String brand;
    public String model;
    public double battery_capacity_kWh;     // Maps to JSON field
    public String fast_charge_port;

    // Optional JSON fields
    public int top_speed_kmh;
    public String battery_type;
    public double torque_nm;
    public int range_km;
    public double acceleration_0_100_s;
    public double fast_charging_power_kw_dc;
    public int seats;
    public String drivetrain;

    // ⚡ NEW: Fields for demo app
    public String name;                     // Combined brand + model
    public double batteryCapacity;          // Alias for battery_capacity_kWh
    public double chargeSpeed;              // Charging speed (can be calculated or set)
    public double currentCharge;            // Current charge percentage (0-100)

    // Default constructor
    public Car() {
        this.currentCharge = 20.0 + (Math.random() * 60); // Random 20-80%
    }

    // Constructor with parameters
    public Car(String brand, String model, double batteryCapacity) {
        this.brand = brand;
        this.model = model;
        this.battery_capacity_kWh = batteryCapacity;
        this.batteryCapacity = batteryCapacity;  // Set both
        this.name = brand + " " + model;
        this.currentCharge = 20.0 + (Math.random() * 60);
    }

    // ⚡ GETTERS
    public double getBatteryCapacity() {
        // Return battery_capacity_kWh if available, otherwise batteryCapacity
        return battery_capacity_kWh > 0 ? battery_capacity_kWh : batteryCapacity;
    }

    public double getCurrentCharge() {
        return currentCharge;
    }

    public String getConnectorType() {
        return fast_charge_port;
    }

    // ⚡ SETTERS
    public void setCurrentCharge(double currentCharge) {
        this.currentCharge = Math.max(0, Math.min(100, currentCharge));
    }

    public void setBatteryCapacity(double capacity) {
        this.batteryCapacity = capacity;
        this.battery_capacity_kWh = capacity;
    }

    public void setChargeSpeed(double chargeSpeed) {
        this.chargeSpeed = chargeSpeed;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Utility methods
    public String getFullName() {
        if (name != null && !name.isEmpty()) {
            return name;
        }
        if (brand != null && model != null) {
            return brand + " " + model;
        }
        return model != null ? model : "Unknown Car";
    }

    public double getRemainingCapacity() {
        double capacity = getBatteryCapacity();
        return capacity * (100 - currentCharge) / 100.0;
    }

    public String getChargeDisplay() {
        return String.format("%.0f%%", currentCharge);
    }

    public String getBatteryStatus() {
        if (currentCharge >= 80) return "🟢 High";
        if (currentCharge >= 50) return "🟡 Medium";
        if (currentCharge >= 20) return "🟠 Low";
        return "🔴 Critical";
    }

    public boolean isCompatibleWith(String portType) {
        if (fast_charge_port == null || portType == null) return true;
        return fast_charge_port.equalsIgnoreCase(portType);
    }

    // Simulation methods (optional - for demo)
    public void simulateDrain(double percentDrained) {
        this.currentCharge = Math.max(0, this.currentCharge - percentDrained);
    }

    public void simulateCharge(double percentCharged) {
        this.currentCharge = Math.min(100, this.currentCharge + percentCharged);
    }
}
