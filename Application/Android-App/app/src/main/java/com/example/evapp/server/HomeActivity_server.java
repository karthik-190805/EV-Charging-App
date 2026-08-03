package com.example.evapp.server;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.evapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

public class HomeActivity_server extends AppCompatActivity implements LocationListener {

    private static final int REQ_LOCATION = 101;
    private MapView mapView;
    private Spinner spinnerStationType;
    private NumberPicker npChargers, npPrice;
    private TimePicker startTimePicker, endTimePicker;
    private ToggleButton toggleStationStatus;
    private Button btnSave;
    private LocationManager locationManager;
    private Marker myMarker;
    private static final String MASTER_FILE = "stations_master.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_activity_home);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid_prefs", Context.MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        // 🗺️ Map setup
        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        IMapController mapController = mapView.getController();
        mapController.setCenter(new GeoPoint(12.9716, 77.5946));
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);

        // 🧩 UI setup
        spinnerStationType = findViewById(R.id.spinnerStationType);
        npChargers = findViewById(R.id.npChargers);
        npPrice = findViewById(R.id.npPrice);
        startTimePicker = findViewById(R.id.startTimePicker);
        endTimePicker = findViewById(R.id.endTimePicker);
        toggleStationStatus = findViewById(R.id.toggleStationStatus);
        btnSave = findViewById(R.id.btnSaveStationInfo);

        // Spinner setup
        String[] types = {"Public", "Private", "Fleet"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
        spinnerStationType.setAdapter(adapter);

        // Number pickers
        npChargers.setMinValue(1);
        npChargers.setMaxValue(20);
        npChargers.setValue(5);

        npPrice.setMinValue(0);
        npPrice.setMaxValue(20);
        npPrice.setValue(10);

        // Time pickers
        startTimePicker.setIs24HourView(false);
        endTimePicker.setIs24HourView(false);
        startTimePicker.setHour(6);
        startTimePicker.setMinute(0);
        endTimePicker.setHour(22);
        endTimePicker.setMinute(0);
        toggleStationStatus.setChecked(true);

        // Initialize location tracking
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        checkLocationPermission();

        btnSave.setOnClickListener(v -> saveStationData());
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 5, this);

            Location lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (lastKnown == null)
                lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (lastKnown != null) {
                onLocationChanged(lastKnown);
            } else {
                // 🟡 Default fallback: Chennai
                GeoPoint chennai = new GeoPoint(13.0827, 80.2707);
                mapView.getController().setZoom(14.0);
                mapView.getController().setCenter(chennai);
                Toast.makeText(this, "Waiting for GPS fix… showing Chennai by default", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        GeoPoint point = new GeoPoint(lat, lon);

        mapView.getController().setZoom(17.0);
        mapView.getController().animateTo(point);

        if (myMarker == null) {
            myMarker = new Marker(mapView);
            myMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            myMarker.setIcon(ContextCompat.getDrawable(this, org.osmdroid.library.R.drawable.person));
            myMarker.setTitle("You are here");
            mapView.getOverlays().add(myMarker);
        }

        myMarker.setPosition(point);
        mapView.invalidate();
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {}
    @Override
    public void onProviderDisabled(@NonNull String provider) {}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    private void saveStationData() {
        String type = spinnerStationType.getSelectedItem().toString();
        int chargers = npChargers.getValue();
        int price = npPrice.getValue();
        boolean isOnline = toggleStationStatus.isChecked();
        String startTime = formatTime(startTimePicker.getHour(), startTimePicker.getMinute());
        String endTime = formatTime(endTimePicker.getHour(), endTimePicker.getMinute());

        try {
            JSONArray stations = loadStationsFromAssets();
            JSONObject station = stations.getJSONObject(0); // Example: STN001
            station.put("type", type);
            station.put("chargers", chargers);
            station.put("price", price);
            station.put("hours", startTime + " - " + endTime);
            station.put("status", isOnline ? "Online" : "Offline");

            FileOutputStream fos = openFileOutput(MASTER_FILE, Context.MODE_PRIVATE);
            fos.write(stations.toString(2).getBytes(StandardCharsets.UTF_8));
            fos.close();

            Toast.makeText(this, "Station updated locally", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving station", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONArray loadStationsFromAssets() throws IOException, JSONException {
        File f = new File(getFilesDir(), MASTER_FILE);
        if (f.exists()) {
            InputStream is = openFileInput(MASTER_FILE);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new JSONArray(new String(buffer, StandardCharsets.UTF_8));
        } else {
            InputStream is = getAssets().open(MASTER_FILE);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            return new JSONArray(new String(buffer, StandardCharsets.UTF_8));
        }
    }

    private String formatTime(int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        int hr = c.get(Calendar.HOUR);
        if (hr == 0) hr = 12;
        String amPm = (c.get(Calendar.AM_PM) == Calendar.AM) ? "AM" : "PM";
        return String.format("%02d:%02d %s", hr, minute, amPm);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            Toast.makeText(this, "Please enable location permission", Toast.LENGTH_LONG).show();
        }
    }

}
