package com.trip.family.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.trip.family.TripApplication
import com.trip.family.api.ApiException
import com.trip.family.api.TripApi
import com.trip.family.data.Trip
import com.trip.family.data.MockTripData
import com.trip.family.data.PackingListResponse
import com.trip.family.data.WeatherDay
import com.trip.family.location.LocationService
import com.trip.family.location.LocationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = (getApplication() as TripApplication).preferences
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _trip = MutableStateFlow<Trip?>(null)
    val trip: StateFlow<Trip?> = _trip.asStateFlow()

    private val _weather = MutableStateFlow<List<WeatherDay>>(emptyList())
    val weather: StateFlow<List<WeatherDay>> = _weather.asStateFlow()

    private val _packingList = MutableStateFlow<PackingListResponse?>(null)
    val packingList: StateFlow<PackingListResponse?> = _packingList.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _hasCache = MutableStateFlow(preferences.hasCachedTrip)
    val hasCache: StateFlow<Boolean> = _hasCache.asStateFlow()

    /** 当前定位城市 */
    private val _currentCity = MutableStateFlow("")
    val currentCity: StateFlow<String> = _currentCity.asStateFlow()

    /** 定位中 */
    private val _isLocating = MutableStateFlow(false)
    val isLocating: StateFlow<Boolean> = _isLocating.asStateFlow()

    /** 错误状态 */
    private val _locationError = MutableStateFlow<String?>(null)
    val locationError: StateFlow<String?> = _locationError.asStateFlow()

    private val _weatherError = MutableStateFlow(false)
    val weatherError: StateFlow<Boolean> = _weatherError.asStateFlow()

    private val _packingError = MutableStateFlow(false)
    val packingError: StateFlow<Boolean> = _packingError.asStateFlow()

    /** 已勾选的行李项 */
    private val _checkedItems = MutableStateFlow<Set<String>>(emptySet())
    val checkedItems: StateFlow<Set<String>> = _checkedItems.asStateFlow()

    /** 事件：网络加载成功后通知导航 */
    private val _navigateToOverview = MutableSharedFlow<Boolean>(extraBufferCapacity = 1)
    val navigateToOverview: SharedFlow<Boolean> = _navigateToOverview.asSharedFlow()

    val serverUrl: String
        get() = preferences.serverUrl

    private var loadJob: Job? = null

    /**
     * 通过分享 token 加载行程（深度链接入口）
     * 只有网络加载成功才触发导航，缓存加载不触发
     */
    fun loadTripByToken(token: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val trip = TripApi.fetchTrip(preferences.serverUrl, token)
                _trip.value = trip
                preferences.lastShareToken = token
                preferences.cachedTrip = trip
                preferences.cachedAt = System.currentTimeMillis()
                _hasCache.value = true
                _navigateToOverview.emit(true)
                // 加载天气和行李（复用 loadJob 的 scope，随其取消）
                launch { loadWeather(trip.tripId) }
                launch { loadPackingList(trip.tripId) }
            } catch (e: ApiException) {
                if (e.statusCode == 404) {
                    _errorMessage.value = "行程不存在或链接已过期"
                } else {
                    _errorMessage.value = "加载失败 (${e.statusCode})"
                }
            } catch (e: Exception) {
                _errorMessage.value = "网络错误: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 刷新当前行程
     */
    fun refreshTrip() {
        val token = preferences.lastShareToken
        if (token.isNotBlank()) {
            loadTripByToken(token)
        }
    }

    /**
     * 加载缓存行程并导航到详情页
     * 同时尝试加载天气和行李（有网则更新，无网则保持空）
     */
    fun loadCachedTrip() {
        val cached = preferences.cachedTrip
        if (cached != null) {
            _trip.value = cached
            viewModelScope.launch {
                _navigateToOverview.emit(true)
                // 尝试刷新天气和行李
                launch { loadWeather(cached.tripId) }
                launch { loadPackingList(cached.tripId) }
            }
        }
    }

    fun updateServerUrl(url: String) {
        preferences.serverUrl = url.trimEnd('/')
    }

    // ─── 定位相关 ───────────────────────────────────────────

    /**
     * 请求定位（需已获取权限）
     * 成功后更新 _currentCity
     */
    fun requestLocation() {
        if (_isLocating.value) return
        _isLocating.value = true
        viewModelScope.launch {
            when (val result = LocationService.getCurrentLocation(getApplication(), fusedLocationClient)) {
                is LocationResult.Success -> {
                    val city = LocationService.reverseGeocode(
                        getApplication(), result.lat, result.lng
                    )
                    _currentCity.value = city
                }
                is LocationResult.Error -> {
                    _currentCity.value = "定位失败"
                    _locationError.value = result.message
                }
            }
            _isLocating.value = false
        }
    }

    fun togglePackingItem(itemId: String) {
        _checkedItems.value = _checkedItems.value.toMutableSet().apply {
            if (contains(itemId)) remove(itemId) else add(itemId)
        }
    }

    fun clearLocationError() { _locationError.value = null }
    fun clearWeatherError() { _weatherError.value = false }
    fun clearPackingError() { _packingError.value = false }

    /**
     * 获取当前定位城市名
     */
    fun getCurrentCity(): String = _currentCity.value

    /**
     * 加载天气（挂载到指定协程 scope，可随父 job 取消）
     */
    suspend fun loadWeather(tripId: String) {
        _weatherError.value = false
        try {
            _weather.value = TripApi.fetchWeather(preferences.serverUrl, tripId)
        } catch (_: Exception) {
            _weather.value = emptyList()
            _weatherError.value = true
        }
    }

    /**
     * 加载行李清单（挂载到指定协程 scope，可随父 job 取消）
     */
    suspend fun loadPackingList(tripId: String) {
        _packingError.value = false
        try {
            _packingList.value = TripApi.fetchPackingList(preferences.serverUrl, tripId)
        } catch (_: Exception) {
            _packingList.value = null
            _packingError.value = true
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * 加载模拟数据（预览用，不依赖后端）
     */
    fun loadMockTrip() {
        _errorMessage.value = null
        _isLoading.value = false
        try {
            _trip.value = MockTripData.getSampleTrip(getApplication())
            viewModelScope.launch {
                _navigateToOverview.emit(true)
            }
        } catch (e: Exception) {
            _errorMessage.value = "模拟数据加载失败: ${e.message}"
        }
    }
}
