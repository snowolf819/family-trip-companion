package com.trip.family.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trip.family.TripPreferences
import com.trip.family.api.ApiException
import com.trip.family.api.TripApi
import com.trip.family.data.Trip
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.MalformedURLException
import java.net.URL

class TripViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = TripPreferences(application)

    private val _trip = MutableStateFlow<Trip?>(null)
    val trip: StateFlow<Trip?> = _trip.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _hasCache = MutableStateFlow(preferences.hasCachedTrip)
    val hasCache: StateFlow<Boolean> = _hasCache.asStateFlow()

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
     * 加载缓存行程（不触发导航）
     */
    fun loadCachedTrip() {
        val cached = preferences.cachedTrip
        if (cached != null) {
            _trip.value = cached
            viewModelScope.launch {
                _navigateToOverview.emit(true)
            }
        }
    }

    fun updateServerUrl(url: String) {
        preferences.serverUrl = url.trimEnd('/')
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
