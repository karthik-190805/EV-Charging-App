package com.example.evapp.server;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.evapp.R;
import com.example.evapp.util.AssetUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class StationDetailActivity_server extends AppCompatActivity {

    // Incoming (from list)
    private String name, type, status;
    private double lat, lng;
    private int chargingPoints; // "No of Charging Points"
    private int totalPorts;
    private int availablePorts;

    // Views
    private TextView tvName, tvAvailability, tvStatusEmoji;
    private TextView tvType, tvLat, tvLng;
    private TextView tvPortsInfo, tvPortStatus;
    private TextView tvPricing, tvHours;

    private final Gson gson = new Gson();
    private final DecimalFormat coordFmt = new DecimalFormat("#.#####");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_station_detail);

        // Initialize views
        tvName = findViewById(R.id.tvName);
        tvAvailability = findViewById(R.id.tvAvailability);
        tvStatusEmoji = findViewById(R.id.tvStatusEmoji);
        tvType = findViewById(R.id.tvType);
        tvLat = findViewById(R.id.tvLat);
        tvLng = findViewById(R.id.tvLng);
        tvPortsInfo = findViewById(R.id.tvPortsInfo);
        tvPortStatus = findViewById(R.id.tvPortStatus);
        tvPricing = findViewById(R.id.tvPricing);
        tvHours = findViewById(R.id.tvHours);

        Button btnBack = findViewById(R.id.btnBack);
        Button btnRoute = findViewById(R.id.btnRoute);
        Button btnShare = findViewById(R.id.btnShare);
        Button btnFeatures = findViewById(R.id.btnFeatures);

        // Read what the adapter sent
        Intent it = getIntent();
        name = it.getStringExtra("name");
        type = it.getStringExtra("type");
        status = it.getStringExtra("status");
        lat = it.getDoubleExtra("lat", 0);
        lng = it.getDoubleExtra("lng", 0);
        chargingPoints = it.getIntExtra("points", 0);
        totalPorts = it.getIntExtra("totalPorts", 0);
        availablePorts = it.getIntExtra("availablePorts", 0);

        // Fill any missing fields from assets
        fillFromAssetsIfMissing();

        // Bind to UI
        tvName.setText(orDash(name));

        // Status display
        setStatusDisplay();

        // Type
        tvType.setText("Charger Type: " + orDash(type));

        // Coordinates
        tvLat.setText("Latitude: " + (lat != 0 ? coordFmt.format(lat) : "-"));
        tvLng.setText("Longitude: " + (lng != 0 ? coordFmt.format(lng) : "-"));

        // Port information
        setPortsDisplay();

        // Pricing (default for server)
        tvPricing.setText("Contact station");

        // Hours (default)
        tvHours.setText("24/7");

        // Actions
        btnBack.setOnClickListener(v -> finish());
        btnRoute.setOnClickListener(v -> openMapRoute());
        btnShare.setOnClickListener(v -> shareStationInfo());
        btnFeatures.setOnClickListener(v -> {
            Intent fx = new Intent(this, FeaturesActivity_server.class);
            fx.putExtra("name", name);
            fx.putExtra("lat", lat);
            fx.putExtra("lng", lng);
            startActivity(fx);
        });
    }

    // ---- Set status emoji and availability text ----
    private void setStatusDisplay() {
        if (totalPorts > 0) {
            // Modern display with port counts
            if (availablePorts == totalPorts) {
                tvStatusEmoji.setText("🟢");
                tvAvailability.setText("All ports available");
                tvAvailability.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (availablePorts == 0) {
                tvStatusEmoji.setText("🔴");
                tvAvailability.setText("All ports occupied");
                tvAvailability.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                tvStatusEmoji.setText("🟡");
                tvAvailability.setText(availablePorts + " of " + totalPorts + " available");
                tvAvailability.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            }
        } else {
            // Legacy display
            if ("Available".equalsIgnoreCase(status) || "Free".equalsIgnoreCase(status)) {
                tvStatusEmoji.setText("🟢");
                tvAvailability.setText("Available");
                tvAvailability.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if ("Occupied".equalsIgnoreCase(status)) {
                tvStatusEmoji.setText("🔴");
                tvAvailability.setText("Occupied");
                tvAvailability.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                tvStatusEmoji.setText("⚪");
                tvAvailability.setText(orDash(status));
                tvAvailability.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        }
    }

    // ---- Set ports display ----
    private void setPortsDisplay() {
        if (totalPorts > 0) {
            // Modern display with port breakdown
            String portInfo = type != null ?
                    String.format(Locale.US, "%d %s Ports", totalPorts, type) :
                    String.format(Locale.US, "%d Charging Ports", totalPorts);

            tvPortsInfo.setText(portInfo);

            String portStatus = String.format(Locale.US,
                    "%d Available • %d Occupied",
                    availablePorts,
                    totalPorts - availablePorts);
            tvPortStatus.setText(portStatus);
        } else if (chargingPoints > 0) {
            // Legacy display
            tvPortsInfo.setText(chargingPoints + " Charging Points");
            tvPortStatus.setText("Status: " + orDash(status));
        } else {
            // No port information
            tvPortsInfo.setText("Port information not available");
            tvPortStatus.setVisibility(View.GONE);
        }
    }

    // ----------------------------- Data fill -----------------------------

    private void fillFromAssetsIfMissing() {
        boolean needType = isBlank(type);
        boolean needLat = lat == 0;
        boolean needLng = lng == 0;
        boolean needPoints = chargingPoints <= 0 && totalPorts <= 0;

        if (!(needType || needLat || needLng || needPoints)) return;

        boolean filled = fillFromJsonByName("ev_features.json");
        if (!filled) filled = fillFromCsvByName("ev_features.csv");
    }

    // ---------- JSON (assets/ev_features.json) ----------

    private boolean fillFromJsonByName(String assetFile) {
        try {
            String json = AssetUtils.readTextAsset(this, assetFile);
            if (json == null || json.trim().isEmpty()) return false;

            JsonElement root = gson.fromJson(json, JsonElement.class);
            JsonArray arr = null;
            if (root.isJsonArray()) {
                arr = root.getAsJsonArray();
            } else if (root.isJsonObject()) {
                JsonObject o = root.getAsJsonObject();
                if (o.has("stations") && o.get("stations").isJsonArray())
                    arr = o.getAsJsonArray("stations");
                else if (o.has("data") && o.get("data").isJsonArray()) arr = o.getAsJsonArray("data");
            }
            if (arr == null || arr.size() == 0) return false;

            String target = norm(name);

            // Exact name
            JsonObject hit = null;
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                JsonObject o = el.getAsJsonObject();
                String nm = getStringAny(o, "name", "station", "station_name", "title");
                if (norm(nm).equals(target)) {
                    hit = o;
                    break;
                }
            }
            // Partial name
            if (hit == null) {
                for (JsonElement el : arr) {
                    if (!el.isJsonObject()) continue;
                    JsonObject o = el.getAsJsonObject();
                    String nm = getStringAny(o, "name", "station", "station_name", "title");
                    String n = norm(nm);
                    if (n.contains(target) || target.contains(n)) {
                        hit = o;
                        break;
                    }
                }
            }
            if (hit == null) return false;

            // Apply only the fields we need
            if (isBlank(type)) {
                String t = getStringAny(hit, "type", "charging_type", "connector", "connector_type", "plug", "socket");
                if (!isBlank(t)) type = t;
            }
            if (lat == 0) {
                lat = getDoubleAny(hit, "latitude", "lat");
            }
            if (lng == 0) {
                lng = getDoubleAny(hit, "longitude", "lng", "lon", "long");
            }
            if (chargingPoints <= 0 && totalPorts <= 0) {
                String p = getStringAny(hit, "No of Charging Points", "no_of_charging_points", "charging_points", "points");
                int val = parseIntSafe(p);
                if (val > 0) {
                    chargingPoints = val;
                    totalPorts = val;
                }
            }
            return true;
        } catch (Exception ignore) {
            return false;
        }
    }

    // ---------- CSV (assets/ev_features.csv) ----------

    private boolean fillFromCsvByName(String assetFile) {
        try (InputStream is = getAssets().open(assetFile);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String headerLine = br.readLine();
            if (headerLine == null) return false;

            String[] headers = parseCsvLine(headerLine);
            Map<Integer, String> keyByCol = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                String k = headerKeyToStd(headers[i]);
                if (k != null) keyByCol.put(i, k);
            }

            String target = norm(name);
            Map<String, String> hit = null;

            String line;
            while ((line = br.readLine()) != null) {
                String[] cols = parseCsvLine(line);
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < cols.length; i++) {
                    String key = keyByCol.get(i);
                    if (key != null) row.put(key, cols[i].trim());
                }
                String nm = row.get("name");
                if (isBlank(nm)) continue;
                String n = norm(nm);
                if (n.equals(target) || n.contains(target) || target.contains(n)) {
                    hit = row;
                    break;
                }
            }
            if (hit == null) return false;

            if (isBlank(type)) {
                String t = firstNonEmpty(hit, "type", "charging_type", "connector", "connector_type", "plug", "socket");
                if (!isBlank(t)) type = t;
            }
            if (lat == 0) {
                lat = parseDoubleSafe(firstNonEmpty(hit, "latitude", "lat"));
            }
            if (lng == 0) {
                lng = parseDoubleSafe(firstNonEmpty(hit, "longitude", "lng", "lon", "long"));
            }
            if (chargingPoints <= 0 && totalPorts <= 0) {
                int pts = parseIntSafe(firstNonEmpty(hit, "No of Charging Points", "no_of_charging_points", "charging_points", "points"));
                if (pts > 0) {
                    chargingPoints = pts;
                    totalPorts = pts;
                }
            }
            return true;

        } catch (Exception ignore) {
            return false;
        }
    }

    // ----------------------------- Buttons -----------------------------

    private void openMapRoute() {
        Uri uri;
        if (lat != 0 || lng != 0) {
            uri = Uri.parse("geo:" + lat + "," + lng + "?q=" + Uri.encode(name));
        } else {
            uri = Uri.parse("geo:0,0?q=" + Uri.encode(name));
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    private void shareStationInfo() {
        String text = "⚡ EV Station: " + orDash(name) +
                "\n📍 Type: " + orDash(type) +
                "\n🔌 Ports: " + (totalPorts > 0 ? (availablePorts + "/" + totalPorts + " available") :
                (chargingPoints > 0 ? chargingPoints + " points" : "-")) +
                "\n📍 Location: " + (lat != 0 ? coordFmt.format(lat) : "-") + ", " +
                (lng != 0 ? coordFmt.format(lng) : "-");

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(share, "Share via"));
    }

    // ----------------------------- Helpers -----------------------------

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty() || s.trim().equals("-");
    }

    private static String orDash(String s) {
        return isBlank(s) ? "-" : s;
    }

    private static String norm(String s) {
        if (s == null) return "";
        String t = s.toLowerCase(Locale.US);
        t = t.replaceAll("[^a-z0-9]+", " ").trim().replaceAll("\\s+", " ");
        return t;
    }

    private static String getStringAny(JsonObject o, String... keys) {
        for (String k : keys) {
            try {
                if (o.has(k) && o.get(k).isJsonPrimitive()) {
                    String v = o.get(k).getAsString();
                    if (v != null && !v.trim().isEmpty()) return v.trim();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static double getDoubleAny(JsonObject o, String... keys) {
        for (String k : keys) {
            try {
                if (o.has(k) && o.get(k).isJsonPrimitive()) {
                    String raw = o.get(k).getAsString().trim().replaceAll("[^0-9.\\-]", "");
                    if (raw.isEmpty()) continue;
                    return Double.parseDouble(raw);
                }
            } catch (Exception ignored) {
            }
        }
        return 0;
    }

    // CSV utils
    private static String[] parseCsvLine(String line) {
        ArrayList<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQ = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '\"') {
                if (inQ && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    cur.append('\"');
                    i++; // escaped quote
                } else {
                    inQ = !inQ;
                }
            } else if (ch == ',' && !inQ) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    private static String headerKeyToStd(String h) {
        String s = norm(h);
        if (s.matches("name|station|station name|title")) return "name";
        if (s.matches("latitude|lat")) return "latitude";
        if (s.matches("longitude|lng|lon|long")) return "longitude";
        if (s.matches("type|charging type|connector|connector type|plug|socket")) return "type";
        if (s.matches("no of charging points|charging points|no_of_charging_points|points"))
            return "No of Charging Points";
        return null;
    }

    private static String firstNonEmpty(Map<String, String> m, String... keys) {
        for (String k : keys) {
            String v = m.get(k);
            if (v != null && !v.trim().isEmpty()) return v.trim();
        }
        return null;
    }

    private static double parseDoubleSafe(String s) {
        if (s == null) return 0;
        try {
            String t = s.replaceAll("[^0-9.\\-]", "");
            if (t.isEmpty()) return 0;
            return Double.parseDouble(t);
        } catch (Exception e) {
            return 0;
        }
    }

    private static int parseIntSafe(String s) {
        if (s == null) return 0;
        try {
            String t = s.replaceAll("[^0-9\\-]", "");
            if (t.isEmpty()) return 0;
            return Integer.parseInt(t);
        } catch (Exception e) {
            return 0;
        }
    }
}
