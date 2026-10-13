package com.example.projekt;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Mushroom.class}, version = 1)
public abstract class MushroomDatabase extends RoomDatabase {
    public abstract MushroomDao mushroomDao();

    private static volatile MushroomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static MushroomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (MushroomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    MushroomDatabase.class, "mushroom_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                MushroomDao dao = INSTANCE.mushroomDao();
                
                dao.insertMushroom(new Mushroom(
                    "Borowik szlachetny", 
                    "Jadalny",
                    "Występuje w lasach iglastych i liściastych, często pod świerkami i dębami. Najbardziej poszukiwany grzyb jadalny w Polsce. Ma gruby, biały trzon i brązowy kapelusz.", 
                    "borowik_szlachetny"
                ));
                
                dao.insertMushroom(new Mushroom(
                    "Podgrzybek brunatny", 
                    "Jadalny",
                    "Rośnie głównie w lasach iglastych i mieszanych, często w mchu. Bardzo smaczny grzyb jadalny. Kapelusz ma barwę od jasnobrązowej do czarnobrązowej.", 
                    "podgrzybek_brunatny"
                ));
                
                dao.insertMushroom(new Mushroom(
                    "Pieprznik jadalny (Kurka)", 
                    "Jadalny",
                    "Można go znaleźć w lasach liściastych i iglastych, często na piaszczystych glebach. Grzyb jadalny o lekko pieprznym smaku i intensywnie żółtej barwie.", 
                    "pieprznik_jadalny"
                ));
                
                dao.insertMushroom(new Mushroom(
                    "Muchomor czerwony", 
                    "Trujący",
                    "Powszechny w lasach całej Polski, pod brzozami i świerkami. Grzyb silnie trujący. Charakterystyczny czerwony kapelusz z białymi kropkami.", 
                    "muchomor_czerwony"
                ));
                
                dao.insertMushroom(new Mushroom(
                    "Maślak zwyczajny", 
                    "Jadalny",
                    "Rośnie niemal wyłącznie pod sosnami. Grzyb jadalny o bardzo lepkiej skórce kapelusza, którą warto zdjąć przed przyrządzeniem.", 
                    "maslak_zwyczajny"
                ));
                
                dao.insertMushroom(new Mushroom(
                    "Czubajka kania", 
                    "Jadalny",
                    "Występuje na brzegach lasów, polanach i w parkach. Wyśmienity grzyb jadalny, często smażony jak sznycel. Ma wielki kapelusz z ruchomym pierścieniem.", 
                    "czubajka_kania"
                ));
                
                dao.insertMushroom(new Mushroom(
                    "Muchomor sromotnikowy", 
                    "Śmiertelnie trujący",
                    "Rośnie w lasach liściastych, szczególnie pod dębami i bukami. Najbardziej trujący grzyb w Polsce, śmiertelnie niebezpieczny. Ma oliwkowozielony kapelusz.", 
                    "muchomor_sromotnikowy"
                ));
                
                dao.insertMushroom(new Mushroom(
                    "Koźlarz babka", 
                    "Jadalny",
                    "Występuje wyłącznie pod brzozami. Bardzo smaczny grzyb jadalny. Ma jasnobrązowy kapelusz i charakterystyczny trzon pokryty czarnymi łuseczkami.", 
                    "kozlarz_babka"
                ));
                
                dao.insertMushroom(new Mushroom(
                    "Mleczaj rydz", 
                    "Jadalny",
                    "Rośnie w lasach iglastych, głównie pod sosnami na piaszczystej glebie. Wysoko ceniony grzyb jadalny, wydziela pomarańczowe mleczko po uszkodzeniu.", 
                    "mleczaj_rydz"
                ));
                
                dao.insertMushroom(new Mushroom(
                    "Gąska zielonka", 
                    "Jadalny",
                    "Można ją spotkać późną jesienią w lasach sosnowych na piaszczystym podłożu. Grzyb jadalny o żółtawym miąższu i specyficznym zapachu świeżej mąki.", 
                    "gaska_zielonka"
                ));
            });
        }
    };
}