package com.example.evapp.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
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

public class StationDetailActivity extends AppCompatActivity {

    // Core fields
    private String name;
    private String type;
    private String status;
    private double lat, lng;
    private int totalPorts;
    private int availablePorts;
    private String pricingText;
    private String operatingHours;
    private int chargingPoints;

    // Views - MATCHING YOUR XML
    private TextView tvName, tvAvailability, tvStatusEmoji;
    private TextView tvType, tvLat, tvLng;
    private TextView tvPortsInfo, tvPortStatus;
    private TextView tvPricing, tvHours;

    private final Gson gson = new Gson();
    private final DecimalFormat coordFmt = new DecimalFormat("#.#####");

    private final androidx.activity.OnBackPressedCallback onBackPressedCallback =
            new androidx.activity.OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    Intent data = new Intent();
                    data.putExtra("focusName", name);
                    setResult(RESULT_OK, data);
                    finish();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_activity_station_detail);

        // Initialize views - MATCHING YOUR XML IDs
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
        Button btnFeat = findViewById(R.id.btnFeatures);

        // Read extras from Intent
        Intent i = getIntent();
        name = i.getStringExtra("name");
        status = i.getStringExtra("status");
        type = i.getStringExtra("type");
        double power = i.getDoubleExtra("power", 0.0);
        lat = i.getDoubleExtra("lat", 0.0);
        lng = i.getDoubleExtra("lng", 0.0);
        totalPorts = i.getIntExtra("totalPorts", 0);
        availablePorts = i.getIntExtra("availablePorts", 0);
        pricingText = i.getStringExtra("pricingText");
        operatingHours = i.getStringExtra("operatingHours");

        chargingPoints = totalPorts;

        // Fallback fill from assets
        fillFromAssetsIfMissing();

        // ---- Bind to UI ----
        tvName.setText(orDash(name));

        // Status emoji and availability
        setStatusDisplay();

        // Type
        String typeText = "Charger Type: ";
        if (!isBlank(type)) {
            if (power > 0) {
                typeText += String.format(Locale.US, "%s (%.0fkW)", type, power);
            } else {
                typeText += type;
            }
        } else {
            typeText += "-";
        }
        tvType.setText(typeText);

        // Coordinates
        tvLat.setText(lat != 0 ? String.format(Locale.US, "Latitude: %.4f", lat) : "Latitude: -");
        tvLng.setText(lng != 0 ? String.format(Locale.US, "Longitude: %.5f", lng) : "Longitude: -");

        // Port information
        setPortsDisplay();

        // Pricing
        if (!isBlank(pricingText) && !pricingText.equals("-")) {
            tvPricing.setText(pricingText);
        } else {
            tvPricing.setText("Not available");
        }

        // Operating hours
        tvHours.setText(!isBlank(operatingHours) ? operatingHours : "24/7");

        // Buttons
        btnBack.setOnClickListener(v -> finish());
        btnRoute.setOnClickListener(v -> openMapRoute());
        btnShare.setOnClickListener(v -> shareStationInfo());
        btnFeat.setOnClickListener(v -> {
            ArrayList<String> features = i.getStringArrayListExtra("features");
            if (features != null && !features.isEmpty()) {
                showFeaturesDialog(features);
            }
        });
        // Register back button handler
        getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

    }

    // Set status emoji and availability text
    private void setStatusDisplay() {
        if (totalPorts > 0) {
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

    // Set ports display
    private void setPortsDisplay() {
        if (totalPorts > 0) {
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
            tvPortsInfo.setText(chargingPoints + " Charging Points");
            tvPortStatus.setText("Status: " + orDash(status));
        } else {
            tvPortsInfo.setText("Port information not available");
            tvPortStatus.setText("");
        }
    }

    // Show features dialog
    private void showFeaturesDialog(ArrayList<String> features) {
        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("🔧 Station Features");

        StringBuilder sb = new StringBuilder();
        for (String feature : features) {
            sb.append("• ").append(feature).append("\n");
        }

        builder.setMessage(sb.toString().trim());
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    // Fallback fill from assets
    private void fillFromAssetsIfMissing() {
        boolean needType = isBlank(type);
        boolean needLat = lat == 0;
        boolean needLng = lng == 0;
        boolean needPoints = chargingPoints <= 0 && totalPorts <= 0;

        if (!(needType || needLat || needLng || needPoints)) return;

        boolean filled = fillFromJsonByName("ev_features.json");
        if (!filled) fillFromCsvByName("ev_features.csv");
    }

    // JSON fill
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
                else if (o.has("data") && o.get("data").isJsonArray())
                    arr = o.getAsJsonArray("data");
            }
            if (arr == null || arr.size() == 0) return false;

            String target = norm(name);
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

    // CSV fill
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
                for (int j = 0; j < cols.length; j++) {
                    String key = keyByCol.get(j);
                    if (key != null) row.put(key, cols[j].trim());
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

    // Buttons
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
                "\n💰 Pricing: " + orDash(pricingText) +
                "\n🕒 Hours: " + orDash(operatingHours) +
                "\n📍 Location: " + (lat != 0 ? coordFmt.format(lat) : "-") + ", " +
                (lng != 0 ? coordFmt.format(lng) : "-");

        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(share, "Share via"));
    }

    // Helpers
    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty() || s.trim().equals("-");
    }

    private static String orDash(String s) {
        return isBlank(s) ? "-" : s;
    }

    private static String norm(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", " ").trim().replaceAll("\\s+", " ");
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
                    if (!raw.isEmpty()) return Double.parseDouble(raw);
                }
            } catch (Exception ignored) {
            }
        }
        return 0;
    }

    private static String[] parseCsvLine(String line) {
        ArrayList<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQ = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '\"') {
                if (inQ && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    cur.append('\"');
                    i++;
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
            return t.isEmpty() ? 0 : Double.parseDouble(t);
        } catch (Exception e) {
            return 0;
        }
    }

    private static int parseIntSafe(String s) {
        if (s == null) return 0;
        try {
            String t = s.replaceAll("[^0-9\\-]", "");
            return t.isEmpty() ? 0 : Integer.parseInt(t);
        } catch (Exception e) {
            return 0;
        }
    }


}
