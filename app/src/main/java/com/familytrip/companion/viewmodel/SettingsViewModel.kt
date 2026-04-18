package com.familytrip.companion.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.familytrip.companion.data.local.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)

    val baseUrl: StateFlow<String> = prefsManager.baseUrl
        .stateIn(viewModelScope, SharingStarted.Eagerly, "https://family-trip-planner.vercel.app")
    val fontSize: StateFlow<String> = prefsManager.fontSize
        .stateIn(viewModelScope, SharingStarted.Eagerly, "medium")
    val darkMode: StateFlow<Boolean> = prefsManager.darkMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun saveBaseUrl(url: String) = viewModelScope.launch {
        try { prefsManager.saveBaseUrl(url) } catch (e: Exception) { Log.e("SettingsVM", "save base url", e) }
    }
    fun saveFontSize(size: String) = viewModelScope.launch {
        try { prefsManager.saveFontSize(size) } catch (e: Exception) { Log.e("SettingsVM", "save font size", e) }
    }
    fun saveDarkMode(enabled: Boolean) = viewModelScope.launch {
        try { prefsManager.saveDarkMode(enabled) } catch (e: Exception) { Log.e("SettingsVM", "save dark mode", e) }
    }
    fun clearCache() = viewModelScope.launch {
        try { prefsManager.clearCache() } catch (e: Exception) { Log.e("SettingsVM", "clear cache", e) }
    }
}
