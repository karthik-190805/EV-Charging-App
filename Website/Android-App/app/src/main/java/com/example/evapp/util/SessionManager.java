package com.example.evapp.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.evapp.model.Car;
import com.google.gson.Gson;

public class SessionManager {

    private static final String PREF_NAME = "com.example.evapp.session";
    private static final String K_NAME    = "name";
    private static final String K_ID      = "id";
    private static final String K_CAR     = "car_json";
    private static final String K_PWD     = "pwd";
    private static final String K_OTP     = "otp";

    private static final String K_FIRST   = "first";
    private static final String K_LAST    = "last";
    private static final String K_EMAIL   = "email";
    private static final String K_MOBILE  = "mobile";
    private static final String K_VEHICLE = "vehicle";

    private final SharedPreferences sp;
    private final Gson gson = new Gson();

    public SessionManager(Context context) {
        sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // --- user basics ---
    public void saveUser(String name, String id, String carJson) {
        sp.edit().putString(K_NAME, name).putString(K_ID, id).putString(K_CAR, carJson).apply();
    }

    public String getName()     { return sp.getString(K_NAME, null); }
    public String getUserId()   { return sp.getString(K_ID, null); }
    public String getCarJson()  { return sp.getString(K_CAR, null); }

    // Helper to get saved car object
    public Car getSavedCar() {
        String json = getCarJson();
        return json == null ? null : gson.fromJson(json, Car.class);
    }

    // --- password/otp (demo only) ---
    public void setPassword(String pwd) {
        sp.edit().putString(K_PWD, pwd).apply();
    }

    public String getPassword() {
        return sp.getString(K_PWD, null);
    }

    public void setOtp(String otp) {
        sp.edit().putString(K_OTP, otp).apply();
    }

    public String getOtp() {
        return sp.getString(K_OTP, null);
    }

    public void clearOtp() {
        sp.edit().remove(K_OTP).apply();
    }

    // --- profile ---
    public void saveProfile(String first, String last, String email, String mobile, String vehicle) {
        sp.edit()
                .putString(K_FIRST, first)
                .putString(K_LAST, last)
                .putString(K_EMAIL, email)
                .putString(K_MOBILE, mobile)
                .putString(K_VEHICLE, vehicle)
                .apply();
    }

    public String getFirst()   { return sp.getString(K_FIRST, ""); }
    public String getLast()    { return sp.getString(K_LAST, ""); }
    public String getEmail()   { return sp.getString(K_EMAIL, ""); }
    public String getMobile()  { return sp.getString(K_MOBILE, ""); }
    public String getVehicle() { return sp.getString(K_VEHICLE, ""); }

    public void logout() {
        sp.edit().clear().apply();
    }

    // ✅ FIXED: Changed 'prefs' to 'sp'
    public void saveDataLoadTime() {
        sp.edit().putLong("data_load_time", System.currentTimeMillis()).apply();
    }

    public long getDataLoadTime() {
        return sp.getLong("data_load_time", 0);
    }

    public String getTimeSinceLastLoad() {
        long loadTime = getDataLoadTime();
        if (loadTime == 0) return "Never";

        long elapsed = System.currentTimeMillis() - loadTime;
        long seconds = elapsed / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) return "Just now";
        if (minutes < 60) return minutes + " min ago";
        if (hours < 24) return hours + " hr ago";
        return days + " day(s) ago";
    }
}
