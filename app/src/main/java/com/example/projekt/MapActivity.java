package com.example.projekt;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
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

        GeoPoint punkt1 = new GeoPoint(50.6865, 19.3980);
        Marker marker1 = new Marker(map);
        marker1.setPosition(punkt1);
        marker1.setTitle("Borowik szlachetny");
        marker1.setSnippet("Znaleziono: 18.10.2026 - pod dębem");
        map.getOverlays().add(marker1);

        GeoPoint punkt2 = new GeoPoint(50.7020, 19.4120);
        Marker marker2 = new Marker(map);
        marker2.setPosition(punkt2);
        marker2.setTitle("Podgrzybek brunatny");
        marker2.setSnippet("Znaleziono: 17.10.2026 - wilgotny mech");
        map.getOverlays().add(marker2);

        map.getController().setZoom(13.5);
        map.getController().setCenter(punkt1);
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