package com.example.projekt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.provider.MediaStore;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private MushroomDatabase database;
    private MushroomDao mushroomDao;

    // Rejestracja launchera dla aparatu
    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Toast.makeText(this, "Zdjęcie zostało zrobione poprawnie!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    // Rejestracja launchera dla galerii
    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    Toast.makeText(this, "Wybrano zdjęcie z galerii!", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicjalizacja bazy danych Room
        database = MushroomDatabase.getDatabase(this);
        mushroomDao = database.mushroomDao();

        // Obsługa przycisku SKANUJ (Aparat)
        findViewById(R.id.btnScan).setOnClickListener(v -> {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                cameraLauncher.launch(cameraIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Nie można otworzyć aparatu", Toast.LENGTH_SHORT).show();
            }
        });

        // Obsługa przycisku GALERIA
        findViewById(R.id.btnGallery).setOnClickListener(v -> {
            galleryLauncher.launch("image/*");
        });

        // Obsługa przycisku ATLAS
        findViewById(R.id.btnAtlas).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AtlasActivity.class);
            startActivity(intent);
        });

        // Obsługa przycisku DZIENNICZEK
        findViewById(R.id.btnDzienniczek).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DzienniczekActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnMap).setOnClickListener(v -> {
            android.net.Uri gmmIntentUri = android.net.Uri.parse("geo:50.6865,19.3980?q=grzyby");
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            try {
                startActivity(mapIntent);
            } catch (Exception e) {
                Toast.makeText(this, "Nie można otworzyć map", Toast.LENGTH_SHORT).show();
            }
        });
    }
}