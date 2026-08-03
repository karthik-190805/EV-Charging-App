package com.example.evapp.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.evapp.R;
import com.example.evapp.util.AssetUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FeaturesActivity extends AppCompatActivity {

    // ---- Views ----
    private TextView tvTitle, tvEmpty;
    private LinearLayout listAvailability, listRestaurants, listCafes, listMedical;

    // ---- Station context (from StationDetailActivity) ----
    private String stationName;
    private double stationLat, stationLng;

    // ---- Data buckets ----
    private final List<String> availability = new ArrayList<>();
    private final List<String> restaurants  = new ArrayList<>();
    private final List<String> cafes        = new ArrayList<>();
    private final List<String> medicals     = new ArrayList<>();

    // Keep parsed JSON objects so we can fall back to a picker
    private final List<JsonObject> allRecords = new ArrayList<>();

    private final Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_features);

        // Bind views (IDs must exist in your existing activity_features.xml)
        tvTitle         = findViewById(R.id.tvTitle);
        tvEmpty         = findViewById(R.id.tvEmpty);
        listAvailability= findViewById(R.id.listAvailability);
        listRestaurants = findViewById(R.id.listRestaurants);
        listCafes       = findViewById(R.id.listCafes);
        listMedical     = findViewById(R.id.listMedical);
        Button btnBack  = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Read station info from Intent
        Intent it = getIntent();
        stationName = it.getStringExtra("name");
        stationLat  = it.getDoubleExtra("lat", 0);
        stationLng  = it.getDoubleExtra("lng", 0);

        tvTitle.setText("Features • " + (stationName == null ? "-" : stationName));

        // Load & render. If not matched, let user pick from JSON list.
        if (!loadAndRender()) {
            showStationPicker();
        }
    }

    // ------------------------------------------------------------------
    // Loading & matching
    // ------------------------------------------------------------------

    /** Loads JSON (assets/ev_features.json), tries to match current station and render. */
    private boolean loadAndRender() {
        JsonObject hit = loadAndMatch();
        if (hit == null) return false;

        extractBuckets(hit);
        return renderSections();
    }

    /** Reads JSON into allRecords and returns the best match by name/coords. */
    private JsonObject loadAndMatch() {
        try {
            String json = AssetUtils.readTextAsset(this, "ev_features.json");
            if (json == null || json.trim().isEmpty()) return null;

            allRecords.clear();

            JsonElement root = gson.fromJson(json, JsonElement.class);
            if (root.isJsonArray()) {
                for (JsonElement e : root.getAsJsonArray()) if (e.isJsonObject()) allRecords.add(e.getAsJsonObject());
            } else if (root.isJsonObject()) {
                JsonObject o = root.getAsJsonObject();
                if (o.has("stations")) {
                    for (JsonElement e : o.getAsJsonArray("stations")) if (e.isJsonObject()) allRecords.add(e.getAsJsonObject());
                } else if (o.has("data")) {
                    for (JsonElement e : o.getAsJsonArray("data")) if (e.isJsonObject()) allRecords.add(e.getAsJsonObject());
                } else {
                    allRecords.add(o);
                }
            }

            if (allRecords.isEmpty()) return null;
            return bestMatch(allRecords, stationName, stationLat, stationLng);

        } catch (Exception e) {
            return null;
        }
    }

    /** Best match: exact normalized name, then contains, then nearest by coordinates. */
    private static JsonObject bestMatch(List<JsonObject> list, String name, double lat, double lng) {
        String target = normalize(name);

        // exact name
        for (JsonObject o : list) {
            if (normalize(optString(o, "name","station","station_name","title")).equals(target)) return o;
        }
        // contains
        for (JsonObject o : list) {
            String n = normalize(optString(o, "name","station","station_name","title"));
            if (!n.isEmpty() && (!target.isEmpty()) && (n.contains(target) || target.contains(n))) return o;
        }
        // nearest by coordinates
        double best = Double.MAX_VALUE;
        JsonObject near = null;
        for (JsonObject o : list) {
            double la = optDouble(o, "latitude","lat");
            double lo = optDouble(o, "longitude","lng","lon","long");
            double d = distanceMeters(la, lo, lat, lng);
            if (d < best) { best = d; near = o; }
        }
        return near;
    }

    // ------------------------------------------------------------------
    // Extract buckets from one JSON object (supports multiple key styles)
    // ------------------------------------------------------------------

    private void extractBuckets(JsonObject o) {
        availability.clear();
        restaurants.clear();
        cafes.clear();
        medicals.clear();

        // Availability heading (NO buttons for this section)
        takeArray(o, availability,
                "availability","availability_status","hours","operating_hours","opening_hours","timings");

        // Nearby buckets (WITH buttons)
        takeArray(o, restaurants, "restaurants","nearby_restaurants","food","dining");
        takeArray(o, cafes,       "cafes","nearby_cafes","coffee","coffee_shops");
        takeArray(o, medicals,    "medical","nearby_medical","nearby_medical_shops","pharmacy","hospitals","clinics");

        // Optional flat fallback: features: ["Nearby restaurants: A, B", ...]
        if (o.has("features") && o.get("features").isJsonArray()) {
            for (JsonElement el : o.getAsJsonArray("features")) {
                if (!el.isJsonPrimitive()) continue;
                String line = el.getAsString();
                if (line == null) continue;
                String lower = line.toLowerCase(Locale.US);
                int colon = line.indexOf(':');
                String values = colon >= 0 ? line.substring(colon + 1).trim() : line.trim();
                String[] parts = values.split("[,\\n]");
                for (String p : parts) {
                    String item = p.trim();
                    if (item.isEmpty()) continue;
                    if (lower.contains("restaurant")) restaurants.add(item);
                    else if (lower.contains("cafe") || lower.contains("coffee")) cafes.add(item);
                    else if (lower.contains("medical") || lower.contains("pharmacy") || lower.contains("hospital")) medicals.add(item);
                    else if (lower.contains("hour") || lower.contains("open")) availability.add(item);
                }
            }
        }
    }

    private static void takeArray(JsonObject obj, List<String> out, String... keys) {
        for (String k : keys) {
            if (!obj.has(k)) continue;
            JsonElement e = obj.get(k);

            if (e.isJsonArray()) {
                JsonArray arr = e.getAsJsonArray();
                for (JsonElement el : arr) {
                    if (el.isJsonPrimitive()) {
                        String v = el.getAsString().trim();
                        if (!v.isEmpty()) out.add(v);
                    } else if (el.isJsonObject()) {
                        String v = optString(el.getAsJsonObject(), "name","title","text");
                        if (!v.isEmpty()) out.add(v);
                    }
                }
            } else if (e.isJsonPrimitive()) {
                String v = e.getAsString().trim();
                if (!v.isEmpty()) out.add(v);
            }
        }
    }

    // ------------------------------------------------------------------
    // Render
    // ------------------------------------------------------------------

    private boolean renderSections() {
        tvEmpty.setVisibility(View.GONE);

        boolean any = false;
        any |= populateSection(listAvailability, availability, false); // no buttons here
        any |= populateSection(listRestaurants, restaurants, true);
        any |= populateSection(listCafes,       cafes, true);
        any |= populateSection(listMedical,     medicals, true);

        if (!any) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No features available for this station.");
        }
        return any;
    }

    /**
     * Inflates row_feature_item (sky-blue rounded card) for each item.
     * For availability section, withActions=false hides the button row.
     */
    private boolean populateSection(LinearLayout container, List<String> items, boolean withActions) {
        if (container == null) return false;
        container.removeAllViews();
        if (items == null || items.isEmpty()) return false;

        for (String item : items) {
            View row = getLayoutInflater().inflate(R.layout.user_row_feature_item, container, false);

            TextView tvTitle = row.findViewById(R.id.tvFeatureTitle);
            View actions     = row.findViewById(R.id.actionsRow);
            Button btnMap    = row.findViewById(R.id.btnMap);
            Button btnRoute  = row.findViewById(R.id.btnRoute);
            Button btnShare  = row.findViewById(R.id.btnShare);

            tvTitle.setText(item);
            actions.setVisibility(withActions ? View.VISIBLE : View.GONE);

            if (withActions) {
                btnMap.setOnClickListener(v -> openMap(item));
                btnRoute.setOnClickListener(v -> openRoute(item));
                btnShare.setOnClickListener(v -> share(item));
            }
            container.addView(row);
        }
        return true;
    }

    // ------------------------------------------------------------------
    // Simple picker dialog if matching fails (no extra layouts required)
    // ------------------------------------------------------------------

    private void showStationPicker() {
        if (allRecords.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("No features file or empty JSON.");
            return;
        }

        List<String> names = new ArrayList<>();
        for (JsonObject o : allRecords) {
            String n = optString(o, "name","station","station_name","title");
            names.add(n.isEmpty() ? "(Unnamed)" : n);
        }

        new AlertDialog.Builder(this)
                .setTitle("Pick a station from ev_features.json")
                .setItems(names.toArray(new String[0]), (d, which) -> {
                    JsonObject chosen = allRecords.get(which);
                    stationName = optString(chosen, "name","station","station_name","title");
                    stationLat  = optDouble(chosen, "latitude","lat");
                    stationLng  = optDouble(chosen, "longitude","lng","lon","long");
                    tvTitle.setText("Features • " + stationName);
                    extractBuckets(chosen);
                    renderSections();
                })
                .setNegativeButton("Cancel", (d, w) -> {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("No features available for this station.");
                })
                .show();
    }

    // ------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------

    private void openMap(String query) {
        Uri uri = (stationLat != 0 || stationLng != 0)
                ? Uri.parse("geo:" + stationLat + "," + stationLng + "?q=" + Uri.encode(query))
                : Uri.parse("geo:0,0?q=" + Uri.encode(query));
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }

    private void openRoute(String query) {
        Uri gmm = Uri.parse("google.navigation:q=" + Uri.encode(query));
        Intent gm = new Intent(Intent.ACTION_VIEW, gmm);
        gm.setPackage("com.google.android.apps.maps");
        try {
            startActivity(gm);
        } catch (Exception e) {
            openMap(query);
        }
    }

    private void share(String item) {
        String text = item + (stationName == null ? "" : " (near " + stationName + ")");
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(i, "Share via"));
    }

    // ------------------------------------------------------------------
    // Small utils
    // ------------------------------------------------------------------

    private static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", " ").trim();
    }

    private static String optString(JsonObject o, String... keys) {
        for (String k : keys) {
            try {
                if (o.has(k) && o.get(k).isJsonPrimitive()) {
                    String v = o.get(k).getAsString();
                    if (!v.trim().isEmpty()) return v.trim();
                }
            } catch (Exception ignored) {}
        }
        return "";
    }

    private static double optDouble(JsonObject o, String... keys) {
        for (String k : keys) {
            try {
                if (o.has(k)) return o.get(k).getAsDouble();
            } catch (Exception ignored) {}
        }
        return 0;
    }

    /** Haversine distance in meters (fallback matching by proximity). */
    private static double distanceMeters(double la, double lo, double lat, double lng) {
        if (la == 0 && lo == 0) return 1e12;
        double R = 6371000.0;
        double dLat = Math.toRadians(la - lat);
        double dLon = Math.toRadians(lo - lng);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat))*Math.cos(Math.toRadians(la))
                * Math.sin(dLon/2)*Math.sin(dLon/2);
        return 2 * R * Math.asin(Math.sqrt(a));
    }
}
