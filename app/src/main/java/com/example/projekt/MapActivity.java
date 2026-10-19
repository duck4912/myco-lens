package com.example.projekt;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapActivity extends AppCompatActivity {

    private MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().setUserAgentValue("MycoLensStudentThesisApp_v2026");
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        setContentView(R.layout.activity_map);

        map = findViewById(R.id.mapView);
        map.setMultiTouchControls(true);
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            finish();
        });

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

        // Marker 1: Borowik szlachetny
        GeoPoint punkt1 = new GeoPoint(50.6865, 19.3980);
        Marker marker1 = new Marker(map);
        marker1.setPosition(punkt1);
        marker1.setTitle("Borowik szlachetny");
        marker1.setSnippet("Znaleziono: 18.10.2026 - pod dębem\n(Kliknij ponownie, aby nawigować)");
        setupMarkerNavigation(marker1);
        map.getOverlays().add(marker1);

        // Marker 2: Podgrzybek brunatny
        GeoPoint punkt2 = new GeoPoint(50.7020, 19.4120);
        Marker marker2 = new Marker(map);
        marker2.setPosition(punkt2);
        marker2.setTitle("Podgrzybek brunatny");
        marker2.setSnippet("Znaleziono: 17.10.2026 - wilgotny mech\n(Kliknij ponownie, aby nawigować)");
        setupMarkerNavigation(marker2);
        map.getOverlays().add(marker2);

        map.getController().setZoom(13.5);
        map.getController().setCenter(punkt1);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }

}