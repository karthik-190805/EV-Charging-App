package com.example.evapp.util;

import com.google.gson.Gson;
import okhttp3.OkHttpClient;

public class ApiClient {
    // Pick ONE base URL depending on how you connect:
    public static final String BASE = "http://10.0.2.2:5000";      // Emulator
    // public static final String BASE = "http://localhost:5000";  // USB + adb reverse
    // public static final String BASE = "http://192.168.1.50:5000"; // Real phone on Wi-Fi (replace IP)

    public static final OkHttpClient HTTP = new OkHttpClient();
    public static final Gson GSON = new Gson();
}
