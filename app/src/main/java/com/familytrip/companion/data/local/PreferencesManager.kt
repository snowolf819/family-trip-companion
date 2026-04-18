package com.familytrip.companion.data.local

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.familytrip.companion.data.model.HistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "companion_settings")

class PreferencesManager(private val context: Context) {

    private object Keys {
        val BASE_URL = stringPreferencesKey("base_url")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val DARK_MODE = stringPreferencesKey("dark_mode")
        val HISTORY = stringSetPreferencesKey("history")
    }

    val baseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.BASE_URL] ?: "https://family-trip-planner.vercel.app"
    }

    val fontSize: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.FONT_SIZE] ?: "medium"
    }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.DARK_MODE] == "true"
    }

    val history: Flow<List<HistoryItem>> = context.dataStore.data.map { prefs ->
        val set = prefs[Keys.HISTORY] ?: emptySet()
        set.mapNotNull { entry ->
            val parts = entry.split("|")
            if (parts.size >= 3) {
                HistoryItem(
                    token = parts[0],
                    title = parts[1],
                    timestamp = parts[2].toLongOrNull() ?: 0L
                )
            } else null
        }.sortedByDescending { it.timestamp }
    }

    suspend fun saveBaseUrl(url: String) {
        context.dataStore.edit { it[Keys.BASE_URL] = url.trimEnd('/') }
    }

    suspend fun saveFontSize(size: String) {
        context.dataStore.edit { it[Keys.FONT_SIZE] = size }
    }

    suspend fun saveDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_MODE] = if (enabled) "true" else "false" }
    }

    suspend fun addHistoryItem(token: String, title: String) {
        try {
            val current = history.first().toMutableList()
            current.removeAll { it.token == token }
            current.add(0, HistoryItem(token, title, System.currentTimeMillis()))
            if (current.size > 20) current.removeLast()
            context.dataStore.edit { prefs ->
                prefs[Keys.HISTORY] = current.map { "${it.token}|${it.title}|${it.timestamp}" }.toSet()
            }
        } catch (e: Exception) {
            Log.e("PreferencesManager", "保存历史记录失败", e)
        }
    }

    suspend fun clearCache() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.HISTORY)
        }
    }
}
