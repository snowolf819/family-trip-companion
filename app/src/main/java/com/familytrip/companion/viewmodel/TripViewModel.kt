package com.familytrip.companion.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.familytrip.companion.data.api.TripApiService
import com.familytrip.companion.data.local.PreferencesManager
import com.familytrip.companion.data.model.HistoryItem
import com.familytrip.companion.data.model.PackingList
import com.familytrip.companion.data.model.Trip
import com.familytrip.companion.data.model.TripDay
import com.familytrip.companion.data.repository.TripRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsManager = PreferencesManager(application)
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private var apiService: TripApiService? = null
    private var cachedBaseUrl: String? = null

    val history: StateFlow<List<HistoryItem>> = prefsManager.history
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _currentTrip = MutableStateFlow<Trip?>(null)
    val currentTrip: StateFlow<Trip?> = _currentTrip.asStateFlow()

    private val _selectedDay = MutableStateFlow<TripDay?>(null)
    val selectedDay: StateFlow<TripDay?> = _selectedDay.asStateFlow()

    private val _packingList = MutableStateFlow<PackingList?>(null)
    val packingList: StateFlow<PackingList?> = _packingList.asStateFlow()

    private val _checkedItems = MutableStateFlow<Set<String>>(emptySet())
    val checkedItems: StateFlow<Set<String>> = _checkedItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private fun getApiService(): TripApiService {
        val baseUrl = try {
            prefsManager.baseUrl.stateIn(viewModelScope, SharingStarted.Eagerly, "").value
        } catch (_: Exception) { "https://family-trip-planner.vercel.app" }

        val effective = baseUrl.ifEmpty { "https://family-trip-planner.vercel.app" }
        if (apiService != null && cachedBaseUrl == effective) return apiService!!

        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(effective.trimEnd('/') + "/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val service = retrofit.create(TripApiService::class.java)
        apiService = service
        cachedBaseUrl = effective
        return service
    }

    fun extractToken(input: String): String {
        val trimmed = input.trim()
        val patterns = listOf(
            "/parent/([A-Za-z0-9]+)".toRegex(),
            "token=([A-Za-z0-9]+)".toRegex(),
            "/share/([A-Za-z0-9]+)".toRegex(),
        )
        for (p in patterns) {
            p.find(trimmed)?.let { return it.groupValues[1] }
        }
        return trimmed
    }

    fun loadTrip(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val repo = TripRepository(getApiService())
            val result = repo.getTripByToken(token)
            _isLoading.value = false
            result.onSuccess { trip ->
                _currentTrip.value = trip
                prefsManager.addHistoryItem(token, trip.title)
            }.onFailure { e ->
                _error.value = e.message ?: "加载失败"
                Log.e("TripViewModel", "加载行程失败", e)
            }
        }
    }

    fun selectDay(day: TripDay) { _selectedDay.value = day }

    fun clearTrip() {
        _currentTrip.value = null
        _selectedDay.value = null
        _packingList.value = null
        _error.value = null
    }

    fun loadPackingList(tripId: String) {
        viewModelScope.launch {
            val repo = TripRepository(getApiService())
            repo.getPackingList(tripId).onSuccess { list ->
                _packingList.value = list
            }.onFailure { e ->
                Log.e("TripViewModel", "加载行李清单失败", e)
            }
        }
    }

    fun toggleItem(itemId: String) {
        _checkedItems.value = if (itemId in _checkedItems.value) {
            _checkedItems.value - itemId
        } else {
            _checkedItems.value + itemId
        }
    }

    fun getCallIntent(phone: String): Intent =
        Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
}
