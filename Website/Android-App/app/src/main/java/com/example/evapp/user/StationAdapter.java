package com.example.evapp.user;

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
import com.example.evapp.model.Station;
import com.example.evapp.util.TimeCalc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StationAdapter extends BaseAdapter {
    private final Context ctx;
    private final List<Station> stations;
    private final Car car;

    // Supplied by HomeActivity from Excel / ev_features.json
    private final Map<String, ArrayList<String>> featuresByNameNorm;
    private final Map<String, ArrayList<String>> featuresByCoordKey;

    public StationAdapter(Context context,
                          List<Station> stations,
                          Car car,
                          Map<String, ArrayList<String>> featuresByNameNorm,
                          Map<String, ArrayList<String>> featuresByCoordKey) {
        this.ctx = context;
        this.stations = stations;
        this.car = car;
        this.featuresByNameNorm = featuresByNameNorm;
        this.featuresByCoordKey = featuresByCoordKey;
    }

    @Override
    public int getCount() {
        return stations.size();
    }

    @Override
    public Object getItem(int position) {
        return stations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(R.layout.user_item_station, parent, false);
        }

        // Find all views
        TextView tvStatusEmoji = convertView.findViewById(R.id.tvStatusEmoji);
        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvAvailability = convertView.findViewById(R.id.tvAvailability);
        TextView tvPortInfo = convertView.findViewById(R.id.tvPortInfo);
        TextView tvPricing = convertView.findViewById(R.id.tvPricing);
        TextView tvDistance = convertView.findViewById(R.id.tvDistance);
        TextView tvETA = convertView.findViewById(R.id.tvETA);
        TextView tvOperatingHours = convertView.findViewById(R.id.tvOperatingHours);

        Station station = stations.get(pos);

        // 1. STATUS EMOJI
        tvStatusEmoji.setText(getStatusEmoji(station));

        // 2. STATION NAME
        tvName.setText(station.name);

        // 3. AVAILABILITY BADGE
        String availText = getAvailabilitySummary(station);
        tvAvailability.setText(availText);

        // Set badge background color
        int bgRes;
        if (availText.equals("Available")) {
            bgRes = R.drawable.badge_available;
        } else if (availText.equals("Occupied")) {
            bgRes = R.drawable.badge_occupied;
        } else {
            bgRes = R.drawable.badge_partial; // "2/3 available"
        }
        tvAvailability.setBackgroundResource(bgRes);

        // 4. PORT INFO
        if (station.ports != null && !station.ports.isEmpty()) {
            tvPortInfo.setText(station.getPortSummary());
        } else {
            tvPortInfo.setText(getPortSpecText(station));
        }

        // 5. PRICING
        if (station.pricing != null) {
            tvPricing.setText("₹" + String.format(Locale.US, "%.1f", station.pricing.baseRate) + "/kWh");
            tvPricing.setVisibility(View.VISIBLE);
        } else {
            tvPricing.setVisibility(View.GONE);
        }

        // 6. DISTANCE
        if (station.distance != null) {
            tvDistance.setText(station.getDistanceDisplay());
            tvDistance.setVisibility(View.VISIBLE);
        } else {
            tvDistance.setVisibility(View.GONE);
        }

        // 7. ETA
        if (station.eta != null) {
            tvETA.setText("• " + station.eta + " min");
            tvETA.setVisibility(View.VISIBLE);
        } else {
            tvETA.setVisibility(View.GONE);
        }

        // 8. OPERATING HOURS
        if (station.operatingHours != null && !station.operatingHours.isEmpty()) {
            tvOperatingHours.setText(station.operatingHours);
        } else {
            tvOperatingHours.setText("24/7");
        }

        // Alternating row colors (optional - can remove if you prefer all white)
        int evenBlue = ContextCompat.getColor(ctx, R.color.sea_blue_verylight);
        int white = ContextCompat.getColor(ctx, android.R.color.white);
        convertView.setBackgroundColor((pos % 2 == 0) ? evenBlue : white);

        // CLICK LISTENER (unchanged - keep your existing logic)
        int mins = (car == null) ? 0 : TimeCalc.minutesToFull(car, station);
        convertView.setOnClickListener(v -> {
            Intent i = new Intent(ctx, StationDetailActivity.class);
            i.putExtra("name", station.name);
            i.putExtra("status", getAvailabilitySummary(station));
            i.putExtra("type", station.type);
            i.putExtra("power", station.power);
            i.putExtra("lat", station.latitude);
            i.putExtra("lng", station.longitude);
            i.putExtra("mins", mins);
            i.putExtra("totalPorts", station.getTotalPortsCount());
            i.putExtra("availablePorts", station.getAvailablePortsCount());

            String pricingText;
            if (station.pricing != null) {
                pricingText = "₹" + String.format(Locale.US, "%.1f", station.pricing.baseRate) + "/kWh";
            } else {
                pricingText = "-";
            }
            i.putExtra("pricingText", pricingText);
            i.putExtra("operatingHours",
                    station.operatingHours != null ? station.operatingHours : "24/7");

            ArrayList<String> feats = lookupFeatures(station.name, station.latitude, station.longitude);
            if (feats != null && !feats.isEmpty()) {
                i.putStringArrayListExtra("features", feats);
            }

            ctx.startActivity(i);
        });

        return convertView;
    }


    // ---------- Availability helpers ----------

    private String getAvailabilitySummary(Station station) {
        if (station.ports != null && !station.ports.isEmpty()) {
            int available = station.getAvailablePortsCount();
            int total = station.getTotalPortsCount();

            if (available == total) return "Available";
            if (available == 0) return "Occupied";
            return available + "/" + total + " available";
        }
        return station.status != null ? station.status : "Unknown";
    }

    private String getStatusEmoji(Station station) {
        if (station.ports != null && !station.ports.isEmpty()) {
            int available = station.getAvailablePortsCount();
            return (available > 0) ? "🟢" : "🔴";
        }
        if ("Free".equalsIgnoreCase(station.status)) return "🟢";
        if ("Occupied".equalsIgnoreCase(station.status)) return "🔴";
        return "⚪";
    }

    private String getPortSpecText(Station station) {
        if (station.ports != null && !station.ports.isEmpty()) {
            return station.getPortSummary();
        }
        String type = (station.type != null && !station.type.isEmpty()) ? station.type : "Type2";
        double power = station.power;
        return "1x " + type + " (" + (int) power + "kW)";
    }

    // ---------- Matching helpers (unchanged) ----------

    private ArrayList<String> lookupFeatures(String rawName, double lat, double lon) {
        String nn = normalizeName(rawName);
        ArrayList<String> feats = safeGet(featuresByNameNorm, nn);
        if (has(feats)) return feats;

        String k3 = coordKey(lat, lon, 3);
        feats = safeGet(featuresByCoordKey, k3);
        if (has(feats)) return feats;

        String k2 = coordKey(lat, lon, 2);
        feats = safeGet(featuresByCoordKey, k2);
        if (has(feats)) return feats;

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

    private static boolean has(List<?> l) {
        return l != null && !l.isEmpty();
    }

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
