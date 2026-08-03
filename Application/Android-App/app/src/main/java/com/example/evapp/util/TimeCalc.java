package com.example.evapp.util;

import com.example.evapp.model.Car;
import com.example.evapp.model.Port;
import com.example.evapp.model.Station;

public class TimeCalc {

    /**
     * Calculate minutes to fully charge a car at a given port
     * @param car The car to charge
     * @param port The charging port/station
     * @return Estimated minutes to full charge
     */
    public static int minutesToFull(Car car, Port port) {
        if (car == null || port == null) return 0;

        // Get car's battery capacity and current charge
        double batteryCapacityKWh = car.getBatteryCapacity(); // e.g., 60 kWh
        double currentChargePercent = car.getCurrentCharge(); // e.g., 20%

        // Calculate energy needed
        double energyNeededKWh = batteryCapacityKWh * (100 - currentChargePercent) / 100.0;

        // Get charging power from port
        double chargingPowerKW = port.power; // e.g., 50 kW

        if (chargingPowerKW <= 0) return 0;

        // Calculate time in hours, then convert to minutes
        double timeHours = energyNeededKWh / chargingPowerKW;
        int timeMinutes = (int) Math.ceil(timeHours * 60);

        return timeMinutes;
    }

    /**
     * Calculate minutes to fully charge a car at a given station (NEW)
     * @param car The car to charge
     * @param station The charging station
     * @return Estimated minutes to full charge
     */
    public static int minutesToFull(Car car, Station station) {
        if (car == null || station == null) return 0;

        // Get car's battery capacity and current charge
        double batteryCapacityKWh = car.getBatteryCapacity();
        double currentChargePercent = car.getCurrentCharge();

        // Calculate energy needed
        double energyNeededKWh = batteryCapacityKWh * (100 - currentChargePercent) / 100.0;

        // Get max charging power from station
        double chargingPowerKW = station.power; // Use station's max power

        // If station has ports, use the highest available power
        if (station.ports != null && !station.ports.isEmpty()) {
            double maxPower = 0;
            for (com.example.evapp.model.ChargerPort port : station.ports) {
                if ("Available".equals(port.status)) {
                    try {
                        // Parse power string like "150kW" or "50kW"
                        String powerStr = port.power.replaceAll("[^0-9.]", "");
                        double power = Double.parseDouble(powerStr);
                        if (power > maxPower) {
                            maxPower = power;
                        }
                    } catch (Exception e) {
                        // Ignore parsing errors
                    }
                }
            }
            if (maxPower > 0) {
                chargingPowerKW = maxPower;
            }
        }

        if (chargingPowerKW <= 0) return 0;

        // Calculate time in hours, then convert to minutes
        double timeHours = energyNeededKWh / chargingPowerKW;
        int timeMinutes = (int) Math.ceil(timeHours * 60);

        return timeMinutes;
    }

    /**
     * Calculate charging cost for a given energy amount and rate
     * @param energyKWh Energy in kilowatt-hours
     * @param ratePerKWh Rate in rupees per kWh
     * @return Total cost in rupees
     */
    public static double calculateCost(double energyKWh, double ratePerKWh) {
        return energyKWh * ratePerKWh;
    }

    /**
     * Calculate energy needed to reach target charge percentage
     * @param car The car
     * @param targetPercent Target charge percentage (0-100)
     * @return Energy needed in kWh
     */
    public static double calculateEnergyNeeded(Car car, double targetPercent) {
        if (car == null) return 0;

        double batteryCapacityKWh = car.getBatteryCapacity();
        double currentChargePercent = car.getCurrentCharge();

        if (targetPercent <= currentChargePercent) return 0;

        return batteryCapacityKWh * (targetPercent - currentChargePercent) / 100.0;
    }

    /**
     * Format time in minutes to readable string
     * @param minutes Time in minutes
     * @return Formatted string (e.g., "1h 30m" or "45 min")
     */
    public static String formatTime(int minutes) {
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

    /**
     * Calculate charging speed efficiency percentage
     * @param actualPowerKW Actual charging power
     * @param maxPowerKW Maximum possible charging power
     * @return Efficiency percentage (0-100)
     */
    public static int calculateEfficiency(double actualPowerKW, double maxPowerKW) {
        if (maxPowerKW <= 0) return 0;
        return (int) Math.min(100, (actualPowerKW / maxPowerKW) * 100);
    }
}
