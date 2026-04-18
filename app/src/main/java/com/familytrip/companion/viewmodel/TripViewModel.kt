package com.familytrip.companion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.familytrip.companion.data.api.TripApiService
import com.familytrip.companion.data.local.PreferencesManager
import com.familytrip.companion.data.model.PackingList
import com.familytrip.companion.data.model.Trip
import com.familytrip.companion.data.model.TripHistory
import com.familytrip.companion.data.repository.TripRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

data class TripUiState(
    val isLoading: Boolean = false,
    val trip: Trip? = null,
    val packingList: PackingList? = null,
    val error: String? = null,
    val history: List<TripHistory> = emptyList()
)

class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val _uiState = MutableStateFlow(TripUiState())
    val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

    private var api: TripApiService? = null
    private var repo: TripRepository? = null

    init {
        viewModelScope.launch {
            prefsManager.tripHistory.collect { history -> _uiState.update { it.copy(history = history) } }
        }
        viewModelScope.launch {
            prefsManager.baseUrl.collect { url -> rebuildApi(url) }
        }
    }

    private fun rebuildApi(baseUrl: String) {
        try {
            val client = OkHttpClient.Builder().connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS).build()
            val retrofit = Retrofit.Builder().baseUrl("$baseUrl/")
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .client(client).build()
            api = retrofit.create(TripApiService::class.java)
            repo = TripRepository(api!!)
        } catch (_: Exception) { }
    }

    fun loadTrip(token: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repo?.getTripByToken(token)
                ?: run { _uiState.update { it.copy(isLoading = false, error = "请先配置服务器地址") }; return@launch }
            result.onSuccess { trip ->
                prefsManager.addHistory(TripHistory(token, trip.title, "${trip.dateRange.start} ~ ${trip.dateRange.end}", System.currentTimeMillis()))
                _uiState.update { it.copy(isLoading = false, trip = trip) }
            }.onFailure { e ->
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "加载失败") }
            }
        }
    }

    fun loadPackingList(tripId: String) {
        viewModelScope.launch {
            val result = repo?.getPackingList(tripId) ?: return@launch
            result.onSuccess { list ->
                _uiState.update { it.copy(packingList = list) }
            }
        }
    }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
