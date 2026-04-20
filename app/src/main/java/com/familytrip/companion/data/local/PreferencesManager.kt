package com.familytrip.companion.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.familytrip.companion.data.model.TripHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "companion_settings")

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_BASE_URL = stringPreferencesKey("base_url")
        private val KEY_FONT_SCALE = floatPreferencesKey("font_scale")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_HISTORY = stringPreferencesKey("trip_history")
        private val json = Json { ignoreUnknownKeys = true }
    }

    val baseUrl: Flow<String> = context.dataStore.data.map { it[KEY_BASE_URL] ?: "" }

    val fontScale: Flow<Float> = context.dataStore.data.map { it[KEY_FONT_SCALE] ?: 1.0f }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { it[KEY_DARK_MODE] ?: false }

    val tripHistory: Flow<List<TripHistory>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_HISTORY] ?: "[]"
        try { json.decodeFromString<List<TripHistory>>(raw) } catch (_: Exception) { emptyList() }
    }

    suspend fun setBaseUrl(url: String) {
        context.dataStore.edit { it[KEY_BASE_URL] = url.trimEnd('/') }
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { it[KEY_FONT_SCALE] = scale }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    suspend fun addHistory(entry: TripHistory) {
        context.dataStore.edit { prefs ->
            val current = try {
                json.decodeFromString<List<TripHistory>>(prefs[KEY_HISTORY] ?: "[]")
            } catch (_: Exception) { emptyList() }
            val updated = (listOf(entry) + current.filter { it.token != entry.token }).take(20)
            prefs[KEY_HISTORY] = json.encodeToString(updated)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit { it[KEY_HISTORY] = "[]" }
    }

    // P1-7: Packing list checked items persistence
    fun getPackingChecked(tripId: String): Flow<Set<String>> {
        val key = stringPreferencesKey("packing_checked_$tripId")
        return context.dataStore.data.map { prefs ->
            val raw = prefs[key] ?: ""
            if (raw.isEmpty()) emptySet() else raw.split(",").toSet()
        }
    }

    suspend fun savePackingChecked(tripId: String, checked: Set<String>) {
        val key = stringPreferencesKey("packing_checked_$tripId")
        context.dataStore.edit { it[key] = checked.joinToString(",") }
    }
}
