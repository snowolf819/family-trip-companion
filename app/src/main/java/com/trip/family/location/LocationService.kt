package com.trip.family.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 定位服务：获取当前位置和城市名
 */
object LocationService {

    /**
     * 检查是否已授予定位权限
     */
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 获取当前位置（经纬度）
     * 需要已在 UI 层获取了权限
     */
    suspend fun getCurrentLocation(
        client: FusedLocationProviderClient
    ): LocationResult {
        return suspendCancellableCoroutine { cont ->
            val cts = CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }

            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { location ->
                    cont.resume(LocationResult.Success(location.latitude, location.longitude))
                }
                .addOnFailureListener { e ->
                    cont.resume(LocationResult.Error(e.message ?: "定位失败"))
                }
        }
    }

    /**
     * 反向地理编码：经纬度 → 城市名
     * 使用 Android 内置 Geocoder（无需网络 API）
     */
    fun reverseGeocode(context: Context, lat: Double, lng: Double): String {
        return try {
            val geocoder = Geocoder(context, java.util.Locale.CHINA)
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                // 优先取城市名
                val city = addresses[0].locality
                    ?: addresses[0].subAdminArea
                    ?: addresses[0].adminArea
                    ?: ""
                // 去掉"市"、"区"等后缀
                city.removeSuffix("市").removeSuffix("区").removeSuffix("县")
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }
    }
}

sealed class LocationResult {
    data class Success(val lat: Double, val lng: Double) : LocationResult()
    data class Error(val message: String) : LocationResult()
}
