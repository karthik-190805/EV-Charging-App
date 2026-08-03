package com.example.evapp.server;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.evapp.R;
import com.example.evapp.model.Car;
import com.example.evapp.model.Port;
import com.example.evapp.util.TimeCalc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StationAdapter_server extends BaseAdapter {
    private final Context ctx;
    private final List<Port> ports;
    private final Car car;

    // Supplied by HomeActivity from Excel
    private final Map<String, ArrayList<String>> featuresByNameNorm;
    private final Map<String, ArrayList<String>> featuresByCoordKey;

    public StationAdapter_server(Context context,
                          List<Port> ports,
                          Car car,
                          Map<String, ArrayList<String>> featuresByNameNorm,
                          Map<String, ArrayList<String>> featuresByCoordKey) {
        this.ctx = context;
        this.ports = ports;
        this.car = car;
        this.featuresByNameNorm = featuresByNameNorm;
        this.featuresByCoordKey = featuresByCoordKey;
    }

    @Override public int getCount() { return ports.size(); }
    @Override public Object getItem(int position) { return ports.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(R.layout.user_row_station, parent, false);
        }
        TextView title = convertView.findViewById(R.id.title);
        TextView subtitle = convertView.findViewById(R.id.subtitle);

        Port p = ports.get(pos);
        title.setText(p.name + " — " + p.status);

        int mins = (car == null) ? 0 : TimeCalc.minutesToFull(car, p);
        String timeTxt = (car == null) ? "No car set" : ("Time to full: " + mins + " min");
        subtitle.setText("Type: " + (p.type == null ? "-" : p.type) +
                " | Power: " + p.power + " kW | " + timeTxt);

        int evenBlue = ContextCompat.getColor(ctx, R.color.sea_blue_verylight);
        int white    = ContextCompat.getColor(ctx, android.R.color.white);
        convertView.setBackgroundColor((pos % 2 == 0) ? evenBlue : white);

        convertView.setOnClickListener(v -> {
            Intent i = new Intent(ctx, StationDetailActivity_server.class);
            i.putExtra("name", p.name);
            i.putExtra("status", p.status);
            i.putExtra("type", p.type);
            i.putExtra("power", p.power);
            i.putExtra("lat", p.latitude);
            i.putExtra("lng", p.longitude);
            i.putExtra("mins", mins);

            ArrayList<String> feats = lookupFeatures(p.name, p.latitude, p.longitude);
            if (feats != null && !feats.isEmpty()) {
                i.putStringArrayListExtra("features", feats);
            }

            ctx.startActivity(i);
        });

        return convertView;
    }

    // ---------- Matching helpers ----------

    private ArrayList<String> lookupFeatures(String rawName, double lat, double lon) {
        // 1) Exact normalized name
        String nn = normalizeName(rawName);
        ArrayList<String> feats = safeGet(featuresByNameNorm, nn);
        if (has(feats)) return feats;

        // 2) Coordinate key ~110m (3 decimals)
        String k3 = coordKey(lat, lon, 3);
        feats = safeGet(featuresByCoordKey, k3);
        if (has(feats)) return feats;

        // 3) Coordinate key ~1.1km (2 decimals)
        String k2 = coordKey(lat, lon, 2);
        feats = safeGet(featuresByCoordKey, k2);
        if (has(feats)) return feats;

        // 4) Fuzzy name match (token Jaccard)
        if (featuresByNameNorm != null && !featuresByNameNorm.isEmpty()) {
            double bestScore = 0.0;
            String bestKey = null;
            String norm = nn;

            for (String key : featuresByNameNorm.keySet()) {
                double s = jaccard(norm, key);
                if (s > bestScore) {
                    bestScore = s;
                    bestKey = key;
                }
            }
            if (bestKey != null && bestScore >= 0.55) {
                feats = featuresByNameNorm.get(bestKey);
                if (has(feats)) return feats;
            }
        }
        return null;
    }

    private static boolean has(List<?> l) { return l != null && !l.isEmpty(); }
    private static <T> T safeGet(Map<String, T> map, String key) {
        if (map == null || key == null) return null;
        return map.get(key);
    }

    private static String normalizeName(String s) {
        if (s == null) return null;
        String t = s.toLowerCase(Locale.US);
        t = t.replaceAll("[^a-z0-9]+", " ").trim().replaceAll("\\s+", " ");
        return t;
    }
    // round to N decimals (3 ≈ 110m, 2 ≈ 1.1km)
    private static String coordKey(double lat, double lon, int dec) {
        String fmt = "%." + dec + "f";
        return String.format(Locale.US, fmt, lat) + "," + String.format(Locale.US, fmt, lon);
    }
    private static double jaccard(String a, String b) {
        if (a == null || b == null) return 0;
        Set<String> ta = new HashSet<>();
        Set<String> tb = new HashSet<>();
        for (String s : a.split(" ")) if (!s.isEmpty()) ta.add(s);
        for (String s : b.split(" ")) if (!s.isEmpty()) tb.add(s);
        if (ta.isEmpty() || tb.isEmpty()) return 0;
        int inter = 0;
        for (String s : ta) if (tb.contains(s)) inter++;
        int union = ta.size() + tb.size() - inter;
        return union == 0 ? 0 : (double) inter / union;
    }
}
