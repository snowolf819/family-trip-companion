package com.trip.family

import android.content.Context
import com.google.gson.Gson
import com.trip.family.data.Trip

class TripPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("trip_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    var serverUrl: String
        get() = prefs.getString("server_url", "https://plan.and.im") ?: "https://plan.and.im"
        set(value) = prefs.edit().putString("server_url", value).apply()

    var lastShareToken: String
        get() = prefs.getString("last_share_token", "") ?: ""
        set(value) = prefs.edit().putString("last_share_token", value).apply()

    var cachedTrip: Trip?
        get() {
            val json = prefs.getString("cached_trip", null) ?: return null
            return try { gson.fromJson(json, Trip::class.java) } catch (_: Exception) { null }
        }
        set(value) {
            val json = gson.toJson(value)
            prefs.edit().putString("cached_trip", json).apply()
        }

    var cachedAt: Long
        get() = prefs.getLong("cached_at", 0L)
        set(value) = prefs.edit().putLong("cached_at", value).apply()

    val hasCachedTrip: Boolean
        get() = prefs.contains("cached_trip") && prefs.getString("last_share_token", null).isNullOrBlank().not()

    fun clearCache() {
        prefs.edit()
            .remove("cached_trip")
            .remove("cached_at")
            .apply()
    }
}
