package com.example.projekt;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "mushrooms")
public class Mushroom {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String nazwa;
    public String jadalnosc;
    public String opis;
    public String nazwaObrazka;

    public Mushroom(String nazwa, String jadalnosc, String opis, String nazwaObrazka) {
        this.nazwa = nazwa;
        this.jadalnosc = jadalnosc;
        this.opis = opis;
        this.nazwaObrazka = nazwaObrazka;
    }
}