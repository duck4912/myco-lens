package com.example.projekt;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
interface MushroomDao {
    @Query("SELECT * FROM mushrooms")
    List<Mushroom> getAllMushrooms();

    @Query("SELECT * FROM mushrooms WHERE id = :id LIMIT 1")
    Mushroom getMushroomById(int id);

    @Insert
    void insertMushroom(Mushroom mushroom);
}