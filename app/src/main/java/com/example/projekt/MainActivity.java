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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = MushroomDatabase.getDatabase(this);
        mushroomDao = database.mushroomDao();

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
}