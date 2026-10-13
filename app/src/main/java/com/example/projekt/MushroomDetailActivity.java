package com.example.projekt;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MushroomDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mushroom_detail);

        // Konfiguracja Toolbara
        Toolbar toolbar = findViewById(R.id.toolbarDetail);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish()); // Powrót do Atlasu

        // Referencje do widoków
        ImageView ivMushroom = findViewById(R.id.ivMushroomDetail);
        TextView tvName = findViewById(R.id.tvMushroomNameDetail);
        TextView tvEdibility = findViewById(R.id.tvMushroomEdibilityDetail);
        TextView tvDescription = findViewById(R.id.tvMushroomDescriptionDetail);

        // Odbieranie ID grzyba
        int mushroomId = getIntent().getIntExtra("MUSHROOM_ID", -1);

        if (mushroomId != -1) {
            // Obsługa kliknięcia w ikonkę maila
            findViewById(R.id.btnShareEmail).setOnClickListener(v -> {
                String name = tvName.getText().toString();
                String description = tvDescription.getText().toString();
                String edibility = tvEdibility.getText().toString();

                String emailBody = "Grzyb: " + name + "\n" +
                                   "Jadalność: " + edibility + "\n\n" +
                                   "Opis:\n" + description;

                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "[" + name + "] - Informacje z aplikacji");
                intent.putExtra(android.content.Intent.EXTRA_TEXT, emailBody);

                startActivity(Intent.createChooser(intent, "Wyślij opis mailem przez..."));
            });

            // Obsługa przycisku wysyłki na konkretny adres
            findViewById(R.id.btnSendToEmail).setOnClickListener(v -> {
                android.widget.EditText etEmail = findViewById(R.id.etEmailAddress);
                String emailAddress = etEmail.getText().toString().trim();

                if (emailAddress.isEmpty()) {
                    android.widget.Toast.makeText(this, "Proszę podać adres e-mail", android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

                String name = tvName.getText().toString();
                String description = tvDescription.getText().toString();

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(android.net.Uri.parse("mailto:")); // Wymusza tylko aplikacje pocztowe
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Informacje o grzybie: " + name);
                intent.putExtra(Intent.EXTRA_TEXT, description);

                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    android.widget.Toast.makeText(this, "Brak aplikacji pocztowej na urządzeniu.", android.widget.Toast.LENGTH_SHORT).show();
                }
            });

            // Pobieranie danych z bazy Room w tle
            new Thread(() -> {
                MushroomDatabase db = MushroomDatabase.getDatabase(this);
                Mushroom mushroom = db.mushroomDao().getMushroomById(mushroomId);

                if (mushroom != null) {
                    // Powrót do wątku UI, aby wyświetlić dane
                    runOnUiThread(() -> {
                        tvName.setText(mushroom.nazwa);
                        tvEdibility.setText(mushroom.jadalnosc);
                        tvDescription.setText(mushroom.opis);

                        // Dynamiczne ładowanie zdjęcia
                        int imageId = getResources().getIdentifier(mushroom.nazwaObrazka, "drawable", getPackageName());
                        if (imageId != 0) {
                            ivMushroom.setImageResource(imageId);
                        } else {
                            ivMushroom.setImageResource(R.drawable.grzyb_glowna);
                        }
                    });
                }
            }).start();
        }
    }
}