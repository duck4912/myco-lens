package com.example.projekt

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object MushroomRepository {
    private const val PREFS_NAME = "mycolens_finds_prefs"
    private const val KEY_FINDS = "saved_finds_json"
    private var isLoaded = false

    val finds = mutableListOf<MushroomFind>()

    fun init(context: Context) {
        if (isLoaded) return
        isLoaded = true

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_FINDS, null)

        if (jsonStr == null) {
            // Domyślne przykładowe znaleziska przy pierwszym uruchomieniu
            finds.add(
                MushroomFind(
                    name = "Borowik szlachetny",
                    date = "18.10.2026",
                    latitude = 50.7061,
                    longitude = 19.3872,
                    description = "Znaleziony pod dębem"
                )
            )
            finds.add(
                MushroomFind(
                    name = "Muchomor czerwony",
                    date = "17.10.2026",
                    latitude = 50.7120,
                    longitude = 19.3950,
                    description = "Wilgotny mech"
                )
            )
            saveToPrefs(context)
        } else {
            val array = JSONArray(jsonStr)
            finds.clear()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                finds.add(
                    MushroomFind(
                        id = obj.optLong("id", System.currentTimeMillis()),
                        name = obj.getString("name"),
                        date = obj.getString("date"),
                        latitude = obj.getDouble("latitude"),
                        longitude = obj.getDouble("longitude"),
                        description = obj.optString("description", ""),
                        photoPath = if (obj.isNull("photoPath")) null else obj.getString("photoPath")
                    )
                )
            }
        }
    }

    fun addFind(context: Context, item: MushroomFind) {
        init(context)
        finds.add(0, item)
        saveToPrefs(context)
    }

    private fun saveToPrefs(context: Context) {
        val array = JSONArray()
        for (item in finds) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("name", item.name)
            obj.put("date", item.date)
            obj.put("latitude", item.latitude)
            obj.put("longitude", item.longitude)
            obj.put("description", item.description)
            obj.put("photoPath", item.photoPath ?: JSONObject.NULL)
            array.put(obj)
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_FINDS, array.toString())
            .apply()
    }
}