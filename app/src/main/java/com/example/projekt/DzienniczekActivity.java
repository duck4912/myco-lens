package com.example.projekt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class DzienniczekActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MushroomRepository.INSTANCE.init(this);
        setContentView(R.layout.activity_dzienniczek);

        Toolbar toolbar = findViewById(R.id.toolbarDzienniczek);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        odswiezListeZnalezisk();
    }

    private void odswiezListeZnalezisk() {
        ViewGroup root = findViewById(android.R.id.content);
        ViewGroup mainLayout = (ViewGroup) root.getChildAt(0);
        if (mainLayout == null) return;

        while (mainLayout.getChildCount() > 1) {
            mainLayout.removeViewAt(1);
        }

        List<MushroomFind> finds = MushroomRepository.INSTANCE.getFinds();

        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        LinearLayout listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        int pad = dpToPx(16);
        listContainer.setPadding(pad, pad, pad, pad);

        for (MushroomFind find : finds) {
            CardView card = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, dpToPx(12));
            card.setLayoutParams(cardParams);
            card.setRadius(dpToPx(12));
            card.setCardElevation(dpToPx(4));
            card.setCardBackgroundColor(Color.WHITE);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

            if (find.getPhotoPath() != null && new File(find.getPhotoPath()).exists()) {
                ImageView iv = new ImageView(this);
                LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(dpToPx(72), dpToPx(72));
                imgParams.setMargins(0, 0, dpToPx(12), 0);
                iv.setLayoutParams(imgParams);
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Bitmap bmp = BitmapFactory.decodeFile(find.getPhotoPath());
                iv.setImageBitmap(bmp);
                row.addView(iv);
            }

            LinearLayout infoCol = new LinearLayout(this);
            infoCol.setOrientation(LinearLayout.VERTICAL);
            infoCol.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            TextView tvName = new TextView(this);
            tvName.setText(find.getName());
            tvName.setTextSize(18f);
            tvName.setTypeface(null, Typeface.BOLD);
            tvName.setTextColor(Color.parseColor("#2E3B23"));

            TextView tvDate = new TextView(this);
            tvDate.setText("📅 " + find.getDate() +
                    (find.getDescription().isEmpty() ? "" : "\n📝 " + find.getDescription()));
            tvDate.setTextSize(14f);
            tvDate.setTextColor(Color.DKGRAY);

            TextView tvCoords = new TextView(this);
            tvCoords.setText(String.format(Locale.ENGLISH, "📍 %.4f, %.4f", find.getLatitude(), find.getLongitude()));
            tvCoords.setTextSize(12f);
            tvCoords.setTextColor(Color.GRAY);

            Button btnShowOnMap = new Button(this);
            btnShowOnMap.setText("Pokaż na mapie");
            btnShowOnMap.setAllCaps(false);
            btnShowOnMap.setOnClickListener(v -> {
                Intent mapIntent = new Intent(DzienniczekActivity.this, MapActivity.class);
                mapIntent.putExtra("FOCUS_LAT", find.getLatitude());
                mapIntent.putExtra("FOCUS_LON", find.getLongitude());
                startActivity(mapIntent);
            });

            infoCol.addView(tvName);
            infoCol.addView(tvDate);
            infoCol.addView(tvCoords);
            infoCol.addView(btnShowOnMap);

            row.addView(infoCol);
            card.addView(row);
            listContainer.addView(card);
        }

        scrollView.addView(listContainer);
        mainLayout.addView(scrollView);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}