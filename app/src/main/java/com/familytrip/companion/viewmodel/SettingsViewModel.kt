package com.familytrip.companion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.familytrip.companion.data.local.PreferencesManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUiState(
    val baseUrl: String = "",
    val fontScale: Float = 1.0f,
    val darkMode: Boolean = false
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefsManager = PreferencesManager(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(prefsManager.baseUrl, prefsManager.fontScale, prefsManager.darkMode) { url, scale, dark ->
                SettingsUiState(url, scale, dark)
            }.collect { _uiState.value = it }
        }
    }

    fun setBaseUrl(url: String) { viewModelScope.launch { prefsManager.setBaseUrl(url) } }
    fun setFontScale(scale: Float) { viewModelScope.launch { prefsManager.setFontScale(scale) } }
    fun setDarkMode(enabled: Boolean) { viewModelScope.launch { prefsManager.setDarkMode(enabled) } }
}
