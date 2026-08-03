package com.example.evapp.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class AssetUtils {

    private static final String TAG = "AssetUtils";

    /**
     * Read text file from assets folder
     */
    public static String readTextAsset(Context context, String filename) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;

        try {
            InputStream is = context.getAssets().open(filename);
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            Log.d(TAG, "Successfully read " + filename + " (" + sb.length() + " characters)");
            return sb.toString();

        } catch (IOException e) {
            Log.e(TAG, "Error reading file: " + filename, e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing reader", e);
                }
            }
        }
    }
}
