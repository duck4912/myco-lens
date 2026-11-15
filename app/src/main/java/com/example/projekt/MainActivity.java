package com.example.projekt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final double DEFAULT_LATITUDE = 50.8118;
    private static final double DEFAULT_LONGITUDE = 19.1203;

    private MushroomDatabase database;
    private MushroomDao mushroomDao;
    private MushroomClassifier classifier;

    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvTemp;
    private TextView tvHumidity;
    private TextView tvLocationName;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null && extras.get("data") instanceof Bitmap) {
                        Bitmap photo = (Bitmap) extras.get("data");
                        rozpoznajGrzyba(photo);
                    }
                }
            }
    );

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        Bitmap bitmap = wczytajBitmapeZUri(uri);
                        rozpoznajGrzyba(bitmap);
                    } catch (IOException e) {
                        Log.e(TAG, "Błąd wczytywania zdjęcia z galerii", e);
                        Toast.makeText(this, "Nie udało się wczytać zdjęcia", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String[]> locationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if ((fineLocationGranted != null && fineLocationGranted) ||
                        (coarseLocationGranted != null && coarseLocationGranted)) {
                    pobierzLokalizacjeIPogode();
                } else {
                    odpytajApiPogodowe(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
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

        // Inicjalizacja modelu TensorFlow Lite
        try {
            classifier = new MushroomClassifier(this);
        } catch (Throwable e) {
            Log.e(TAG, "Błąd ładowania modelu TFLite", e);
            Toast.makeText(this, "Błąd modelu: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        sprawdzUprawnieniaIPobierzPogode();
        skonfigurujPrzyciski();
    }

    private Bitmap wczytajBitmapeZUri(Uri uri) throws IOException {
        Bitmap rawBitmap;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
            rawBitmap = ImageDecoder.decodeBitmap(source);
        } else {
            rawBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        }
        return rawBitmap.copy(Bitmap.Config.ARGB_8888, false);
    }

    private void rozpoznajGrzyba(Bitmap bitmap) {
        if (classifier == null) {
            Toast.makeText(this, "Model AI nie jest gotowy", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Analizuję zdjęcie...", Toast.LENGTH_SHORT).show();

        Executors.newSingleThreadExecutor().execute(() -> {
            Bitmap argbBitmap = bitmap.getConfig() == Bitmap.Config.ARGB_8888
                    ? bitmap
                    : bitmap.copy(Bitmap.Config.ARGB_8888, false);

            MushroomClassifier.ClassificationResult result = classifier.classify(argbBitmap);

            runOnUiThread(() -> pokazWynikKlasyfikacji(result));
        });
    }

    private void pokazWynikKlasyfikacji(MushroomClassifier.ClassificationResult result) {
        String sformatowanaNazwa = formatujNazweGatunku(result.label);
        float procent = result.confidence * 100f;

        String ostrzezenie;
        if (result.label.contains("muchomor")) {
            ostrzezenie = "\n\n⚠️ UWAGA: Wykryto gatunek potencjalnie trujący! Nie spożywać!";
        } else if (procent < 70f) {
            ostrzezenie = "\n\n⚠️ Niska pewność rozpoznania (<70%). Zweryfikuj gatunek w Atlasie lub z atlasem mykologicznym.";
        } else {
            ostrzezenie = "\n\nℹ️ Wynik ma charakter pomocniczy. Przed spożyciem zawsze upewnij się co do cech gatunkowych.";
        }

        new AlertDialog.Builder(this)
                .setTitle("Wynik analizy MycoLens")
                .setMessage(String.format(Locale.getDefault(),
                        "Rozpoznany gatunek:\n%s\n\nPewność modelu: %.1f%%%s",
                        sformatowanaNazwa, procent, ostrzezenie))
                .setPositiveButton("Otwórz Atlas", (dialog, which) ->
                        startActivity(new Intent(MainActivity.this, AtlasActivity.class)))
                .setNegativeButton("Zamknij", null)
                .show();
    }

    private String formatujNazweGatunku(String rawLabel) {
        switch (rawLabel) {
            case "borowik_szlachetny": return "Borowik szlachetny";
            case "czubajka_kania": return "Czubajka kania";
            case "gaska_zielonka": return "Gąska zielonka";
            case "kozlarz_babka": return "Koźlarz babka";
            case "maslak_zwyczajny": return "Maślak zwyczajny";
            case "mleczaj_rydz": return "Mleczaj rydz";
            case "muchomor_czerwonawy":
            case "muchomor_czerwony": return "Muchomor czerwony";
            case "muchomor_sromotnikowy": return "Muchomor sromotnikowy";
            case "pieprznik_jadalny": return "Pieprznik jadalny (Kurka)";
            case "podgrzybek_brunatny": return "Podgrzybek brunatny";
            default:
                String clean = rawLabel.replace("_", " ");
                return clean.substring(0, 1).toUpperCase() + clean.substring(1);
        }
    }

    private void skonfigurujPrzyciski() {
        findViewById(R.id.btnScan).setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                cameraLauncher.launch(cameraIntent);
            } catch (Exception e) {
                Log.e(TAG, "Nie można otworzyć aparatu", e);
                Toast.makeText(this, "Brak dostępnego aparatu na urządzeniu", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.btnGallery).setOnClickListener(v -> galleryLauncher.launch("image/*"));

        findViewById(R.id.btnAtlas).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AtlasActivity.class)));

        findViewById(R.id.btnDzienniczek).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, DzienniczekActivity.class)));

        findViewById(R.id.btnMap).setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, MapActivity.class)));
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
                                odpytajApiPogodowe(DEFAULT_LATITUDE, DEFAULT_LONGITUDE);
                            }
                        });
                    }
                });
    }

    private void odpytajApiPogodowe(double lat, double lon) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                String miasto = pobierzNazweMiejscowosci(lat, lon);

                String urlStr = String.format(Locale.ENGLISH,
                        "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&current=temperature_2m,relative_humidity_2m",
                        lat, lon);

                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                StringBuilder result = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                }

                JSONObject json = new JSONObject(result.toString());
                JSONObject current = json.getJSONObject("current");
                double temp = current.getDouble("temperature_2m");
                int humidity = current.getInt("relative_humidity_2m");

                runOnUiThread(() -> zaktualizujWidokPogody(temp, humidity, miasto));

            } catch (Exception e) {
                Log.e(TAG, "Błąd pobierania danych pogodowych z Open-Meteo", e);
            }
        });
    }

    private String pobierzNazweMiejscowosci(double lat, double lon) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                if (address.getLocality() != null) {
                    return address.getLocality();
                } else if (address.getSubAdminArea() != null) {
                    return address.getSubAdminArea();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Nie udało się pobrać nazwy miejscowości z koordynatów", e);
        }
        return "Moja okolica";
    }

    private void zaktualizujWidokPogody(double temp, int humidity, String miasto) {
        if (tvTemp != null) {
            tvTemp.setText(String.format(Locale.getDefault(), "🌡️ %.1f°C", temp));
        }
        if (tvHumidity != null) {
            tvHumidity.setText(String.format(Locale.getDefault(), "💧 WIL. %d%%", humidity));
        }
        if (tvLocationName != null) {
            tvLocationName.setText("📍 " + miasto);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (classifier != null) {
            classifier.close();
        }
    }
}