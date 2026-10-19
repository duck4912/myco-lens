package com.example.projekt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.Executors;
import android.location.Address;
import android.location.Geocoder;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MushroomDatabase database;
    private MushroomDao mushroomDao;

    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvTemp;
    private TextView tvHumidity;
    private TextView tvLocationName;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Toast.makeText(this, "Zdjęcie zostało zrobione poprawnie!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    Toast.makeText(this, "Wybrano zdjęcie z galerii!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<String[]> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (fineLocationGranted != null && fineLocationGranted) {
                    pobierzLokalizacjeIPogode();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    pobierzLokalizacjeIPogode();
                } else {
                    odpytajApiPogodowe(50.8118, 19.1203);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTemp = findViewById(R.id.tvTemp);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvLocationName = findViewById(R.id.tvLocationName);

        database = MushroomDatabase.getDatabase(this);
        mushroomDao = database.mushroomDao();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        sprawdzUprawnieniaIPobierzPogode();

        findViewById(R.id.btnScan).setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                cameraLauncher.launch(cameraIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Nie można otworzyć aparatu", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnGallery).setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
        });

        findViewById(R.id.btnAtlas).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AtlasActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnDzienniczek).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DzienniczekActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnMap).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });
    }

    private void sprawdzUprawnieniaIPobierzPogode() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            pobierzLokalizacjeIPogode();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void pobierzLokalizacjeIPogode() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        odpytajApiPogodowe(location.getLatitude(), location.getLongitude());
                    } else {
                        fusedLocationClient.getLastLocation().addOnSuccessListener(this, lastLoc -> {
                            if (lastLoc != null) {
                                odpytajApiPogodowe(lastLoc.getLatitude(), lastLoc.getLongitude());
                            } else {
                                odpytajApiPogodowe(50.8118, 19.1203);
                            }
                        });
                    }
                });
    }

    private void odpytajApiPogodowe(double lat, double lon) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String nazwaMiasta = "Moja okolica";
                try {
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        if (address.getLocality() != null) {
                            nazwaMiasta = address.getLocality(); // np. Częstochowa, Olsztyn
                        } else if (address.getSubAdminArea() != null) {
                            nazwaMiasta = address.getSubAdminArea();
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "Błąd operacji", e);
                }

                String urlStr = String.format(Locale.ENGLISH,
                        "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m",
                        lat, lon);

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(result.toString());
                JSONObject current = json.getJSONObject("current");
                double temp = current.getDouble("temperature_2m");
                int humidity = current.getInt("relative_humidity_2m");

                final String finalMiasto = nazwaMiasta;

                runOnUiThread(() -> {
                    if (tvTemp != null) {
                        tvTemp.setText(String.format(Locale.getDefault(), "🌡️ %.1f°C", temp));
                    }
                    if (tvHumidity != null) {
                        tvHumidity.setText(String.format(Locale.getDefault(), "💧 WIL. %d%%", humidity));
                    }
                    if (tvLocationName != null) {
                        tvLocationName.setText("📍 " + finalMiasto);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}