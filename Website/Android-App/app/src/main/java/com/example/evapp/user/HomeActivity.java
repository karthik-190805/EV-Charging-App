package com.example.evapp.user;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.evapp.R;
import com.example.evapp.model.Car;
import com.example.evapp.model.Station;
import com.example.evapp.model.Stationfeature;
import com.example.evapp.util.AssetUtils;
import com.example.evapp.util.NetworkUtil;
import com.example.evapp.util.SafeDoubleTypeAdapter;
import com.example.evapp.util.SafeStringTypeAdapter;
import com.example.evapp.util.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class HomeActivity extends AppCompatActivity {

    private static final int RC_LOCATION = 42;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final String TAG = "HomeActivity";

    private MapView map;
    private ListView listView;
    private StationAdapter adapter;
    private RadiusMarkerClusterer markerCluster;
    private ProgressBar progressBar;

    private final List<Station> stations = new ArrayList<>();
    private final List<Station> stationsAll = new ArrayList<>();

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Double.class, new SafeDoubleTypeAdapter())
            .registerTypeAdapter(double.class, new SafeDoubleTypeAdapter())
            .registerTypeAdapter(String.class, new SafeStringTypeAdapter())
            .create();

    private final Map<String, ArrayList<String>> featuresByNameNorm = new HashMap<>();
    private final Map<String, ArrayList<String>> featuresByCoordKey = new HashMap<>();

    private Car myCar;

    private CheckBox cbUseMyLocation;
    private EditText etStart, etDestination;
    private Button btnSetRoute, btnClear, btnRefresh;
    private TextView tvLastUpdated, tvNetworkStatus;

    private Polyline currentRouteLine;
    private GeoPoint origin;
    private GeoPoint destination;

    private LocationManager lm;
    private Runnable pendingLocationTask = null;

    private FusedLocationProviderClient fusedLocationClient;
    private Location userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.user_activity_home);

        // Initialize views
        map = findViewById(R.id.map);
        listView = findViewById(R.id.listViewStations);
        cbUseMyLocation = findViewById(R.id.cbUseMyLocation);
        etStart = findViewById(R.id.etStart);
        etDestination = findViewById(R.id.etDestination);
        btnSetRoute = findViewById(R.id.btnSetRoute);
        btnClear = findViewById(R.id.btnClear);
        btnRefresh = findViewById(R.id.btnRefresh);
        tvLastUpdated = findViewById(R.id.tvLastUpdated);
        tvNetworkStatus = findViewById(R.id.tvNetworkStatus);
        progressBar = findViewById(R.id.progressBar);

        // Configure map
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setMultiTouchControls(true);

        // Initialize marker clustering
        markerCluster = new RadiusMarkerClusterer(this);
        markerCluster.setRadius(100);
        map.getOverlays().add(markerCluster);

        // Get saved user car
        SessionManager session = new SessionManager(this);
        myCar = session.getSavedCar();

        lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Display network status
        updateNetworkStatus();

        // Request location first, then load stations
        requestLocationAndLoadStations();

        loadFeaturesFromJson();

        // Display last update time
        updateLastLoadedTime();

        // Checkbox listener
        cbUseMyLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etStart.setEnabled(!isChecked);
        });

        // Button listeners
        btnSetRoute.setOnClickListener(v -> setRoute());
        btnClear.setOnClickListener(v -> clearRoute());

        // Refresh button
        btnRefresh.setOnClickListener(v -> {
            requestLocationAndLoadStations();
            loadFeaturesFromJson();
            updateLastLoadedTime();
            Toast.makeText(this, "Data refreshed!", Toast.LENGTH_SHORT).show();
        });
    }

    // Loading indicator helpers
    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    // Request location permission and load stations
    private void requestLocationAndLoadStations() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationAndLoad();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        }
    }

    // Get current location
    private void getCurrentLocationAndLoad() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            loadStationsWithoutLocation();
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        userLocation = location;
                        loadStationsWithLocation(location.getLatitude(), location.getLongitude());
                    } else {
                        Location lastKnown = getBestLastKnown();
                        if (lastKnown != null) {
                            userLocation = lastKnown;
                            loadStationsWithLocation(lastKnown.getLatitude(), lastKnown.getLongitude());
                        } else {
                            loadStationsWithoutLocation();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get location", e);
                    loadStationsWithoutLocation();
                });
    }

    // Load stations WITH distance calculation
    private void loadStationsWithLocation(double userLat, double userLon) {
        showLoading();
        new Thread(() -> {
            try {
                String json = AssetUtils.readTextAsset(this, "stations.json");
                Log.d(TAG, "JSON length = " + (json == null ? 0 : json.length()));

                java.lang.reflect.Type t = new TypeToken<List<Station>>() {}.getType();
                List<Station> stationList = gson.fromJson(json, t);

                if (stationList != null) {
                    Log.d(TAG, "Loaded " + stationList.size() + " stations");

                    for (Station station : stationList) {
                        if (station.hasValidLocation()) {
                            station.calculateDistance(userLat, userLon);
                        }
                    }

                    Collections.sort(stationList, (s1, s2) -> {
                        if (s1.distance == null && s2.distance == null) return 0;
                        if (s1.distance == null) return 1;
                        if (s2.distance == null) return -1;
                        return Integer.compare(s1.distance, s2.distance);
                    });

                    runOnUiThread(() -> {
                        hideLoading();
                        stationsAll.clear();
                        stationsAll.addAll(stationList);

                        stations.clear();
                        stations.addAll(stationsAll);

                        if (adapter == null) {
                            adapter = new StationAdapter(this, stations, myCar, featuresByNameNorm, featuresByCoordKey);
                            listView.setAdapter(adapter);
                        } else {
                            adapter.notifyDataSetChanged();
                        }

                        renderMarkers(stationsAll);
                        showEmptyState();

                        GeoPoint userPos = new GeoPoint(userLat, userLon);
                        map.getController().setZoom(12.0);
                        map.getController().setCenter(userPos);

                        Toast.makeText(this, "Showing " + stationList.size() + " stations (nearest first)", Toast.LENGTH_SHORT).show();
                    });
                }

            } catch (Exception e) {
                Log.e(TAG, "Error loading stations with location", e);
                runOnUiThread(() -> {
                    hideLoading();
                    loadStationsWithoutLocation();
                });
            }
        }).start();
    }

    // Load stations WITHOUT distance calculation
    private void loadStationsWithoutLocation() {
        showLoading();
        new Thread(() -> {
            try {
                String json = AssetUtils.readTextAsset(this, "stations.json");
                Log.d(TAG, "JSON length = " + (json == null ? 0 : json.length()));

                java.lang.reflect.Type t = new TypeToken<List<Station>>() {}.getType();
                List<Station> stationList = gson.fromJson(json, t);

                runOnUiThread(() -> {
                    hideLoading();
                    stationsAll.clear();
                    if (stationList != null) {
                        stationsAll.addAll(stationList);
                    }

                    stations.clear();
                    stations.addAll(stationsAll);

                    if (adapter == null) {
                        adapter = new StationAdapter(this, stations, myCar, featuresByNameNorm, featuresByCoordKey);
                        listView.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }

                    renderMarkers(stationsAll);
                    showEmptyState();

                    if (!stationsAll.isEmpty()) {
                        GeoPoint center = new GeoPoint(stationsAll.get(0).latitude, stationsAll.get(0).longitude);
                        map.getController().setZoom(11.0);
                        map.getController().setCenter(center);
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error loading stations without location", e);
                runOnUiThread(this::hideLoading);
            }
        }).start();
    }

    private void loadFeaturesFromJson() {
        try {
            String json = AssetUtils.readTextAsset(this, "ev_features.json");
            java.lang.reflect.Type t = new TypeToken<List<Stationfeature>>() {}.getType();
            List<Stationfeature> list = gson.fromJson(json, t);

            featuresByNameNorm.clear();
            featuresByCoordKey.clear();

            if (list != null) {
                for (Stationfeature e : list) {
                    if (e == null) continue;
                    if (e.name == null || e.features == null || e.features.isEmpty()) continue;

                    String nn = normalizeName(e.name);
                    if (nn != null && !nn.isEmpty()) {
                        featuresByNameNorm.put(nn, new ArrayList<>(e.features));
                    }
                    String ck3 = coordKey(e.latitude, e.longitude, 3);
                    featuresByCoordKey.put(ck3, new ArrayList<>(e.features));
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error loading ev_features.json", ex);
        }
    }

    private void updateNetworkStatus() {
        String networkType = NetworkUtil.getNetworkTypeName(this);
        tvNetworkStatus.setText("📡 " + networkType);

        if (!NetworkUtil.isOnline(this)) {
            tvNetworkStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        } else {
            tvNetworkStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
        }
    }

    private void updateLastLoadedTime() {
        SessionManager session = new SessionManager(this);
        String lastUpdate = session.getTimeSinceLastLoad();
        tvLastUpdated.setText("Updated: " + lastUpdate);
    }

    private void setRoute() {
        String destTxt = etDestination.getText().toString().trim();
        boolean useCurrent = cbUseMyLocation.isChecked();
        String startTxt = etStart.getText().toString().trim();

        if (destTxt.isEmpty()) {
            Toast.makeText(this, "Enter destination", Toast.LENGTH_SHORT).show();
            return;
        }

        if (useCurrent) {
            ensureLocationAndRun(() -> {
                requestFreshLocation(loc -> {
                    if (loc == null) {
                        Toast.makeText(this, "Couldn't get current location yet", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    origin = new GeoPoint(loc.getLatitude(), loc.getLongitude());

                    geocodeOneAsync(destTxt, result -> {
                        if (result == null) {
                            Toast.makeText(this, "Destination not found", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        destination = result;
                        onOriginDestinationReady();
                    });
                });
            });
        } else {
            if (startTxt.isEmpty()) {
                Toast.makeText(this, "Enter start or select current location", Toast.LENGTH_SHORT).show();
                return;
            }

            geocodeOneAsync(startTxt, originResult -> {
                if (originResult == null) {
                    Toast.makeText(this, "Start not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                origin = originResult;

                geocodeOneAsync(destTxt, destResult -> {
                    if (destResult == null) {
                        Toast.makeText(this, "Destination not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    destination = destResult;
                    onOriginDestinationReady();
                });
            });
        }
    }

    private void geocodeOneAsync(String query, Consumer<GeoPoint> callback) {
        if (!NetworkUtil.isOnline(this)) {
            runOnUiThread(() -> {
                showNetworkError();
                callback.accept(null);
            });
            return;
        }

        new Thread(() -> {
            try {
                Geocoder gc = new Geocoder(this, Locale.getDefault());
                List<Address> list = gc.getFromLocationName(query, 1);

                if (list == null || list.isEmpty()) {
                    runOnUiThread(() -> callback.accept(null));
                    return;
                }

                Address a = list.get(0);
                GeoPoint result = new GeoPoint(a.getLatitude(), a.getLongitude());
                runOnUiThread(() -> callback.accept(result));

            } catch (IOException e) {
                Log.e(TAG, "Geocoding failed", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Geocoding failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    callback.accept(null);
                });
            }
        }).start();
    }

    private void showNetworkError() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("⚠️ No Internet Connection")
                .setMessage("Some features require internet:\n\n• Route planning\n• Real-time updates\n• Station search\n\nOffline: You can still view cached stations.")
                .setPositiveButton("OK", null)
                .setNeutralButton("Retry", (dialog, which) -> requestLocationAndLoadStations())
                .show();
    }

    private void onOriginDestinationReady() {
        drawRoute(java.util.Arrays.asList(origin, destination));

        double corridorMeters = 2000.0;
        List<Station> visible = new ArrayList<>();

        for (Station station : stationsAll) {
            GeoPoint pt = new GeoPoint(station.latitude, station.longitude);
            if (isPointNearSegment(origin, destination, pt, corridorMeters)) {
                station.calculateDistance(origin.getLatitude(), origin.getLongitude());
                visible.add(station);
            }
        }

        Collections.sort(visible, (a, b) -> {
            if (a.distance == null) return 1;
            if (b.distance == null) return -1;
            return Integer.compare(a.distance, b.distance);
        });

        stations.clear();
        stations.addAll(visible);
        adapterNotifySafe();
        renderMarkers(stations);
        showEmptyState();

        map.getController().setZoom(12.0);
        map.getController().setCenter(origin);

        Toast.makeText(this, "Showing " + visible.size() + " stations (nearest first)", Toast.LENGTH_SHORT).show();
    }

    private void clearRoute() {
        origin = null;
        destination = null;

        map.getOverlays().clear();
        map.getOverlays().add(markerCluster);

        if (userLocation != null) {
            for (Station station : stationsAll) {
                if (station.hasValidLocation()) {
                    station.calculateDistance(userLocation.getLatitude(), userLocation.getLongitude());
                }
            }
        }

        if (!stationsAll.isEmpty()) {
            GeoPoint center = userLocation != null
                    ? new GeoPoint(userLocation.getLatitude(), userLocation.getLongitude())
                    : new GeoPoint(stationsAll.get(0).latitude, stationsAll.get(0).longitude);
            map.getController().setZoom(11.0);
            map.getController().setCenter(center);
        }

        stations.clear();
        stations.addAll(stationsAll);
        adapterNotifySafe();
        renderMarkers(stationsAll);
        showEmptyState();
        map.invalidate();

        etStart.setText("");
        etDestination.setText("");
        Toast.makeText(this, "Route cleared.", Toast.LENGTH_SHORT).show();
    }

    private void renderMarkers(List<Station> list) {
        map.getOverlays().clear();

        if (currentRouteLine != null)
            map.getOverlays().add(currentRouteLine);

        markerCluster.getItems().clear();

        for (Station station : list) {
            Marker marker = new Marker(map);
            marker.setPosition(new GeoPoint(station.latitude, station.longitude));
            marker.setTitle(station.name);

            StationInfoWindow infoWindow = new StationInfoWindow(
                    R.layout.station_info_window,
                    map,
                    station,
                    this
            );
            marker.setInfoWindow(infoWindow);

            if (station.hasAvailablePort()) {
                marker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_station_available));
            } else {
                marker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_station_occupied));
            }

            marker.setOnMarkerClickListener((clickedMarker, mapView) -> {
                InfoWindow.closeAllInfoWindowsOn(map);
                clickedMarker.showInfoWindow();
                return true;
            });

            markerCluster.add(marker);
        }

        map.getOverlays().add(markerCluster);
        map.invalidate();
    }

    private void drawRoute(List<GeoPoint> pts) {
        if (currentRouteLine != null)
            map.getOverlayManager().remove(currentRouteLine);

        currentRouteLine = new Polyline();
        currentRouteLine.setPoints(pts);
        currentRouteLine.setWidth(8f);
        currentRouteLine.setColor(ContextCompat.getColor(this, R.color.sea_blue));
        currentRouteLine.setGeodesic(true);

        map.getOverlays().add(currentRouteLine);
    }

    private void requestFreshLocation(Consumer<Location> callback) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.accept(null);
            return;
        }

        lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                callback.accept(location);
            }
        }, Looper.getMainLooper());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Location fallback = getBestLastKnown();
            callback.accept(fallback);
        }, 10000);
    }

    private void ensureLocationAndRun(Runnable onReady) {
        boolean granted =
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (granted) {
            onReady.run();
        } else {
            pendingLocationTask = onReady;
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    RC_LOCATION);
        }
    }

    private Location getBestLastKnown() {
        try {
            List<String> providers = lm.getProviders(true);
            Location best = null;
            for (String p : providers) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return null;
                }
                Location l = lm.getLastKnownLocation(p);
                if (l == null) continue;
                if (best == null || l.getAccuracy() < best.getAccuracy()) best = l;
            }
            return best;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] perms, @NonNull int[] res) {
        super.onRequestPermissionsResult(requestCode, perms, res);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            boolean granted = res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                Toast.makeText(this, "Location permission granted!", Toast.LENGTH_SHORT).show();
                getCurrentLocationAndLoad();
            } else {
                Toast.makeText(this, "Location permission denied. Showing stations without distance.", Toast.LENGTH_LONG).show();
                loadStationsWithoutLocation();
            }
        }

        if (requestCode == RC_LOCATION) {
            boolean granted = res.length > 0 && res[0] == PackageManager.PERMISSION_GRANTED;

            if (granted) {
                Toast.makeText(this, "Location permission granted!", Toast.LENGTH_SHORT).show();

                if (pendingLocationTask != null) {
                    pendingLocationTask.run();
                    pendingLocationTask = null;
                }
            } else {
                Toast.makeText(this, "Location permission denied. Cannot use 'My Location' feature.", Toast.LENGTH_LONG).show();

                boolean shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                        this, Manifest.permission.ACCESS_FINE_LOCATION);

                if (!shouldShowRationale) {
                    showPermissionSettingsDialog();
                }
            }
        }
    }

    private void showPermissionSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Location Permission Required")
                .setMessage("To use 'My Location', please grant location permission in Settings.\n\nSettings → Apps → EVApp → Permissions → Location")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Empty state handling
    private void showEmptyState() {
        if (stations.isEmpty()) {
            listView.setVisibility(View.GONE);

            TextView emptyView = findViewById(android.R.id.empty);
            if (emptyView == null) {
                emptyView = new TextView(this);
                emptyView.setId(android.R.id.empty);  // Use Android's built-in ID
                emptyView.setText("🔍 No stations found\n\nTry:\n• Adjusting your route\n• Clearing filters\n• Checking your location");
                emptyView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                emptyView.setTextSize(16);
                emptyView.setPadding(32, 100, 32, 32);
                emptyView.setTextColor(getResources().getColor(android.R.color.darker_gray));

                ViewGroup parent = (ViewGroup) listView.getParent();
                parent.addView(emptyView);
            }
            emptyView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            TextView emptyView = findViewById(android.R.id.empty);
            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
            }
        }
    }


    // Geometry helpers
    private static double distancePointToSegmentMeters(GeoPoint a, GeoPoint b, GeoPoint p) {
        double lat0 = Math.toRadians((a.getLatitude() + b.getLatitude()) / 2.0);
        double mPerDegLat = 111132.92 - 559.82 * Math.cos(2 * lat0) + 1.175 * Math.cos(4 * lat0);
        double mPerDegLon = 111412.84 * Math.cos(lat0) - 93.5 * Math.cos(3 * lat0);

        double ax = a.getLongitude() * mPerDegLon, ay = a.getLatitude() * mPerDegLat;
        double bx = b.getLongitude() * mPerDegLon, by = b.getLatitude() * mPerDegLat;
        double px = p.getLongitude() * mPerDegLon, py = p.getLatitude() * mPerDegLat;

        double vx = bx - ax, vy = by - ay;
        double wx = px - ax, wy = py - ay;

        double c1 = vx * wx + vy * wy;
        if (c1 <= 0) return Math.hypot(px - ax, py - ay);
        double c2 = vx * vx + vy * vy;
        if (c2 <= c1) return Math.hypot(px - bx, py - by);

        double t = c1 / c2;
        double projx = ax + t * vx, projy = ay + t * vy;
        return Math.hypot(px - projx, py - projy);
    }

    private static boolean isPointNearSegment(GeoPoint a, GeoPoint b, GeoPoint p, double tolMeters) {
        return distancePointToSegmentMeters(a, b, p) <= tolMeters;
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

    private void adapterNotifySafe() {
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}
