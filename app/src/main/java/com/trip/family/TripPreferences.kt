package com.trip.family

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.trip.family.data.Trip

class TripPreferences(context: Context) {

    private val gson = Gson()

    // 明文 prefs：仅存非敏感设置
    private val settingsPrefs: SharedPreferences =
        context.getSharedPreferences("trip_settings", Context.MODE_PRIVATE)

    // 加密 prefs：存行程缓存、token 等敏感数据
    private val securePrefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "trip_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // 加密失败时 fallback 到普通 prefs（部分设备不支持 AES256-GCM）
        context.getSharedPreferences("trip_secure_prefs_fallback", Context.MODE_PRIVATE)
    }

    private val CACHE_MAX_AGE_MS = 24 * 60 * 60 * 1000L // 24 小时

    var serverUrl: String
        get() = settingsPrefs.getString("server_url", "https://plan.and.im") ?: "https://plan.and.im"
        set(value) = settingsPrefs.edit().putString("server_url", value).apply()

    var lastShareToken: String
        get() = securePrefs.getString("last_share_token", "") ?: ""
        set(value) = securePrefs.edit().putString("last_share_token", value).apply()

    var cachedTrip: Trip?
        get() {
            val json = securePrefs.getString("cached_trip", null) ?: return null
            return try { gson.fromJson(json, Trip::class.java) } catch (_: Exception) { null }
        }
        set(value) {
            if (value == null) {
                securePrefs.edit().remove("cached_trip").remove("cached_at").apply()
            } else {
                securePrefs.edit()
                    .putString("cached_trip", gson.toJson(value))
                    .putLong("cached_at", System.currentTimeMillis())
                    .apply()
            }
        }

    var cachedAt: Long
        get() = securePrefs.getLong("cached_at", 0L)
        set(value) = securePrefs.edit().putLong("cached_at", value).apply()

    /** 字体缩放比例：1.0 标准，1.2 大字，1.4 超大字 */
    var fontScale: Float
        get() = settingsPrefs.getFloat("font_scale", 1.0f)
        set(value) = settingsPrefs.edit().putFloat("font_scale", value.coerceIn(1.0f, 1.4f)).apply()

    val hasCachedTrip: Boolean
        get() = securePrefs.contains("cached_trip") && securePrefs.getString("last_share_token", null).isNullOrBlank().not()

    /** 缓存是否已过期 */
    val isCacheExpired: Boolean
        get() {
            if (!hasCachedTrip) return true
            return System.currentTimeMillis() - cachedAt > CACHE_MAX_AGE_MS
        }

    /** 缓存距今多久（人类可读） */
    val cacheAgeFormatted: String
        get() {
            if (cachedAt <= 0L) return ""
            val diff = System.currentTimeMillis() - cachedAt
            val minutes = diff / 60_000
            val hours = diff / 3_600_000
            val days = diff / 86_400_000
            return when {
                days > 0 -> "${days}天前"
                hours > 0 -> "${hours}小时前"
                minutes > 0 -> "${minutes}分钟前"
                else -> "刚刚"
            }
        }

    fun clearCache() {
        securePrefs.edit()
            .remove("cached_trip")
            .remove("cached_at")
            .apply()
    }
}
