package com.example.projekt;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class AtlasActivity extends AppCompatActivity {

    private MushroomAdapter adapter;
    private MushroomDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atlas);

        Toolbar toolbar = findViewById(R.id.toolbarAtlas);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Inicjalizacja RecyclerView
        RecyclerView recyclerView = findViewById(R.id.rvAtlas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Pusta lista na start
        adapter = new MushroomAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        // Baza danych
        database = MushroomDatabase.getDatabase(this);

        // Pobieranie danych w tle
        loadMushrooms();
    }

    private void loadMushrooms() {
        new Thread(() -> {
            List<Mushroom> mushrooms = database.mushroomDao().getAllMushrooms();
            // Powrót do wątku UI, aby zaktualizować listę
            runOnUiThread(() -> {
                adapter.setMushrooms(mushrooms);
            });
        }).start();
    }
}