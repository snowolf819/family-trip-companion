package com.familytrip.companion.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.familytrip.companion.data.api.TripApiService
import com.familytrip.companion.data.local.PreferencesManager
import com.familytrip.companion.data.model.PackingList
import com.familytrip.companion.data.model.Trip
import com.familytrip.companion.data.model.TripHistory
import com.familytrip.companion.data.model.WeatherInfo
import com.familytrip.companion.data.repository.TripRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.net.SocketException
import java.net.UnknownHostException

data class TripUiState(
    val isLoading: Boolean = false,
    val trip: Trip? = null,
    val packingList: PackingList? = null,
    val packingError: String? = null,
    val isPackingLoading: Boolean = false,
    val weather: List<WeatherInfo> = emptyList(),
    val error: String? = null,
    val history: List<TripHistory> = emptyList()
)

class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    private val _uiState = MutableStateFlow(TripUiState())
    val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

    val baseUrl: StateFlow<String> = prefsManager.baseUrl.stateIn(viewModelScope, SharingStarted.Lazily, "")

    private var cachedClient: OkHttpClient? = null
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
        if (baseUrl.isBlank()) { api = null; repo = null; return }
        try {
            val client = cachedClient ?: OkHttpClient.Builder()
                .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build().also { cachedClient = it }
            val retrofit = Retrofit.Builder().baseUrl("$baseUrl/")
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .client(client).build()
            api = retrofit.create(TripApiService::class.java)
            repo = TripRepository(api!!)
        } catch (_: Exception) { }
    }

    private fun friendlyError(e: Throwable): String = when (e) {
        is UnknownHostException, is SocketException -> "网络不可用，请检查网络连接"
        is java.net.SocketTimeoutException -> "连接超时，请稍后重试"
        else -> e.message ?: "加载失败"
    }

    fun loadTrip(token: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val repository = repo
            if (repository == null) {
                _uiState.update { it.copy(isLoading = false, error = "请先在设置中配置服务器地址") }
                return@launch
            }
            repository.getTripByToken(token)
                .onSuccess { trip ->
                    val title = trip.title.ifBlank { "未命名行程" }
                    prefsManager.addHistory(TripHistory(token, title, "${trip.dateRange.start} ~ ${trip.dateRange.end}", System.currentTimeMillis()))
                    _uiState.update { it.copy(isLoading = false, trip = trip) }
                    loadWeather(trip.tripId)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = friendlyError(e)) }
                }
        }
    }

    fun loadWeather(tripId: String) {
        viewModelScope.launch {
            val service = api ?: return@launch
            try {
                val w = service.getWeather(tripId)
                _uiState.update { it.copy(weather = w) }
            } catch (_: Exception) { }
        }
    }

    fun loadPackingList(tripId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPackingLoading = true, packingError = null) }
            val repository = repo
            if (repository == null) {
                _uiState.update { it.copy(isPackingLoading = false, packingError = "请先配置服务器地址") }
                return@launch
            }
            repository.getPackingList(tripId)
                .onSuccess { list -> _uiState.update { it.copy(isPackingLoading = false, packingList = list) } }
                .onFailure { e -> _uiState.update { it.copy(isPackingLoading = false, packingError = friendlyError(e)) } }
        }
    }

    fun clearHistory() { viewModelScope.launch { prefsManager.clearHistory() } }
    fun clearError() { _uiState.update { it.copy(error = null) } }
}
