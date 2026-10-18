package com.example.projekt

data class MushroomFind(

    val id: Long = System.currentTimeMillis(),
    val name: String,
    val date: String,
    val latitude: Double,
    val longitude: Double,
    val description: String = "",
    val photoPath: String? = null
)