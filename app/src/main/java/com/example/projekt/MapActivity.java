package com.example.projekt;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapActivity extends AppCompatActivity {

    private MapView map;
    private MyLocationNewOverlay myLocationOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue("MycoLensStudentThesisApp_v2026");
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_map);

        map = findViewById(R.id.mapView);
        map.setMultiTouchControls(true);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        map.setTileSource(new XYTileSource(
                "OpenTopoMap",
                0, 17, 256, ".png",
                new String[]{
                        "https://a.tile.opentopomap.org/",
                        "https://b.tile.opentopomap.org/",
                        "https://c.tile.opentopomap.org/"
                }
        ));

        map.getTileProvider().clearTileCache();

        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), map);

        float density = getResources().getDisplayMetrics().density;
        int outerSize = Math.round(20 * density);

        Bitmap locationBitmap = Bitmap.createBitmap(outerSize, outerSize, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(locationBitmap);
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setAntiAlias(true);
        paint.setColor(android.graphics.Color.WHITE);
        canvas.drawCircle(outerSize / 2f, outerSize / 2f, outerSize / 2f, paint);
        paint.setColor(android.graphics.Color.parseColor("#0088FF"));
        canvas.drawCircle(outerSize / 2f, outerSize / 2f, (outerSize / 2f) - (2 * density), paint);
        myLocationOverlay.setPersonIcon(locationBitmap);
        myLocationOverlay.setDirectionIcon(locationBitmap);
        myLocationOverlay.setDrawAccuracyEnabled(true);

        myLocationOverlay.enableMyLocation();
        map.getOverlays().add(myLocationOverlay);

        findViewById(R.id.btnMyLocation).setOnClickListener(v -> {
            if (myLocationOverlay != null && myLocationOverlay.getMyLocation() != null) {
                map.getController().animateTo(myLocationOverlay.getMyLocation());
                map.getController().setZoom(16.0);
            } else {
                Toast.makeText(this, "Oczekiwanie na sygnał GPS...", Toast.LENGTH_SHORT).show();
            }
        });

        Drawable mushroomPin = getCustomMushroomMarker();

        // Marker 1: Borowik szlachetny
        GeoPoint punkt1 = new GeoPoint(50.6865, 19.3980);
        Marker marker1 = new Marker(map);
        marker1.setPosition(punkt1);
        marker1.setTitle("Borowik szlachetny");
        marker1.setSnippet("Znaleziono: 18.10.2026 - pod dębem\n(Kliknij ponownie, aby nawigować)");
        marker1.setIcon(mushroomPin);
        marker1.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        setupMarkerNavigation(marker1);
        map.getOverlays().add(marker1);

        // Marker 2: Podgrzybek brunatny
        GeoPoint punkt2 = new GeoPoint(50.7020, 19.4120);
        Marker marker2 = new Marker(map);
        marker2.setPosition(punkt2);
        marker2.setTitle("Podgrzybek brunatny");
        marker2.setSnippet("Znaleziono: 17.10.2026 - wilgotny mech\n(Kliknij ponownie, aby nawigować)");
        marker2.setIcon(mushroomPin);
        marker2.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        setupMarkerNavigation(marker2);
        map.getOverlays().add(marker2);

        map.getController().setZoom(13.5);
        map.getController().setCenter(punkt1);
        map.invalidate();
    }

    private Drawable getCustomMushroomMarker() {
        Bitmap rawBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pinezka);
        if (rawBitmap != null) {
            float density = getResources().getDisplayMetrics().density;
            int sizePx = Math.round(26 * density);
            Bitmap scaled = Bitmap.createScaledBitmap(rawBitmap, sizePx, sizePx, true);
            return new BitmapDrawable(getResources(), scaled);
        }
        return ContextCompat.getDrawable(this, R.drawable.pinezka);
    }

    private void setupMarkerNavigation(Marker marker) {
        marker.setOnMarkerClickListener((m, mapView) -> {
            if (m.isInfoWindowShown()) {
                double lat = m.getPosition().getLatitude();
                double lon = m.getPosition().getLongitude();

                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + lat + "," + lon);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                try {
                    startActivity(mapIntent);
                } catch (Exception e) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + lat + "," + lon));
                    startActivity(browserIntent);
                }
            } else {
                m.showInfoWindow();
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
        if (myLocationOverlay != null) myLocationOverlay.enableMyLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) map.onPause();
        if (myLocationOverlay != null) myLocationOverlay.disableMyLocation();
    }
}