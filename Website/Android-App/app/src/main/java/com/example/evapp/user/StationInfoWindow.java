package com.example.evapp.user;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.evapp.R;
import com.example.evapp.model.ChargerPort;
import com.example.evapp.model.Station;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class StationInfoWindow extends InfoWindow {

    private Station station;
    private Context context;

    public StationInfoWindow(int layoutResId, MapView mapView, Station station, Context context) {
        super(layoutResId, mapView);
        this.station = station;
        this.context = context;
    }

    @Override
    public void onOpen(Object item) {
        closeAllInfoWindowsOn(mMapView);

        View view = mView;

        // Header - Station name
        TextView tvStationName = view.findViewById(R.id.tvStationName);
        tvStationName.setText(station.name);

        // Set availability icon
        int availableCount = station.getAvailablePortsCount();
        if (availableCount > 0) {
            tvStationName.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_station_available, 0, 0, 0);
        } else {
            tvStationName.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.ic_station_occupied, 0, 0, 0);
        }

        // Distance and ETA
        TextView tvDistanceEta = view.findViewById(R.id.tvDistanceEta);
        if (station.distance != null && station.eta != null) {
            tvDistanceEta.setText("📍 " + station.getDistanceDisplay() + " away • ~" + station.eta + " min");
            tvDistanceEta.setVisibility(View.VISIBLE);
        } else {
            tvDistanceEta.setVisibility(View.GONE);
        }

        // Port Availability Section
        TextView tvPortAvailabilityHeader = view.findViewById(R.id.tvPortAvailabilityHeader);
        tvPortAvailabilityHeader.setText("🔌 Port Availability:");

        LinearLayout llPortList = view.findViewById(R.id.llPortList);
        llPortList.removeAllViews();

        if (station.ports != null && !station.ports.isEmpty()) {
            for (ChargerPort port : station.ports) {
                TextView tvPort = new TextView(context);
                tvPort.setText(getPortDisplayText(port));
                tvPort.setTextSize(14);
                tvPort.setPadding(8, 4, 8, 4);

                // Color coding based on status
                if ("Available".equals(port.status)) {
                    tvPort.setTextColor(Color.parseColor("#4CAF50")); // Green
                } else if ("Occupied".equals(port.status)) {
                    tvPort.setTextColor(Color.parseColor("#F44336")); // Red
                } else {
                    tvPort.setTextColor(Color.parseColor("#FF9800")); // Orange
                }

                llPortList.addView(tvPort);
            }
        } else {
            TextView tvNoPort = new TextView(context);
            tvNoPort.setText("No port information available");
            tvNoPort.setTextSize(14);
            tvNoPort.setPadding(8, 4, 8, 4);
            tvNoPort.setTextColor(Color.GRAY);
            llPortList.addView(tvNoPort);
        }

        // Pricing
        TextView tvPricing = view.findViewById(R.id.tvPricing);
        if (station.pricing != null) {
            tvPricing.setText("💰 " + station.pricing.getFormattedRate() + " | ⏰ " + station.operatingHours);
            tvPricing.setVisibility(View.VISIBLE);
        } else {
            tvPricing.setVisibility(View.GONE);
        }

        // Amenities
        TextView tvAmenities = view.findViewById(R.id.tvAmenities);
        if (station.features != null && !station.features.isEmpty()) {
            String amenitiesText = "✨ " + String.join(" • ", station.features);
            tvAmenities.setText(amenitiesText);
            tvAmenities.setVisibility(View.VISIBLE);
        } else {
            tvAmenities.setVisibility(View.GONE);
        }

        // Tap for details
        TextView tvTapDetails = view.findViewById(R.id.tvTapDetails);
        tvTapDetails.setText("[Tap for full details]");

        // Make entire popup clickable
        view.setOnClickListener(v -> {
            Intent intent = new Intent(context, StationDetailActivity.class);
            intent.putExtra("station_name", station.name);
            intent.putExtra("station_lat", station.latitude);
            intent.putExtra("station_lon", station.longitude);
            context.startActivity(intent);
        });
    }

    @Override
    public void onClose() {
        // Cleanup if needed
    }

    /**
     * Format port display text with status icons
     */
    private String getPortDisplayText(ChargerPort port) {
        String icon;
        if ("Available".equals(port.status)) {
            icon = "✅";
        } else if ("Occupied".equals(port.status)) {
            icon = "🔴";
        } else if ("Under Maintenance".equals(port.status)) {
            icon = "⚠️";
        } else {
            icon = "⚪";
        }

        return String.format("%s - %s (%s): %s %s",
                port.portId,
                port.connectorType,
                port.power,
                icon,
                port.status);
    }
}
