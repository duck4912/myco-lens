package com.example.projekt

object MushroomRepository {
    val finds = mutableListOf<MushroomFind>(
        MushroomFind(
            name = "Borowik szlachetny",
            date = "18.10.2026",
            latitude = 50.7061,
            longitude = 19.3872,
            description = "Znaleziony pod dębem"
        ),
        MushroomFind(
            name = "Muchomor czerwony",
            date = "17.10.2026",
            latitude = 50.7120,
            longitude = 19.3950,
            description = "Wilgotny mech"
        )
    )

    fun addFind(item: MushroomFind) {
        finds.add(item)
    }
}