package com.trip.family.api

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.trip.family.data.PackingListResponse
import com.trip.family.data.Trip
import com.trip.family.data.WeatherDay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLEncoder

object TripApi {

    private val gson = Gson()

    private fun validateBaseUrl(baseUrl: String) {
        val url = try {
            URL(baseUrl)
        } catch (e: MalformedURLException) {
            throw ApiException(-1, "服务器地址格式不正确")
        }
        val host = url.host ?: ""
        when (url.protocol) {
            "https" -> { /* ok */ }
            "http" -> {
                val allowed = host == "localhost" || host == "127.0.0.1"
                        || host.startsWith("10.0.") || host.startsWith("192.168.")
                if (!allowed) throw ApiException(-1, "非安全连接：请使用 HTTPS")
            }
            else -> throw ApiException(-1, "不支持的协议：${url.protocol}")
        }
    }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8")

    private suspend fun fetch(url: String): String = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/json")
        connection.connect()
        try {
            val code = connection.responseCode
            if (code == 200) {
                connection.inputStream.bufferedReader().readText()
            } else {
                throw ApiException(code, connection.errorStream?.bufferedReader()?.readText() ?: "HTTP $code")
            }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * 通过分享 token 获取行程数据
     * GET {baseUrl}/api/trips/share/{token}
     */
    suspend fun fetchTrip(baseUrl: String, token: String): Trip {
        val cleanBase = baseUrl.trimEnd('/')
        validateBaseUrl(cleanBase)
        if (token.isBlank()) throw ApiException(-1, "分享码不能为空")

        val url = "$cleanBase/api/trips/share/${encode(token)}"
        val json = fetch(url)
        val trip = gson.fromJson(json, Trip::class.java)
        if (trip.tripId.isBlank() && trip.title.isBlank()) {
            throw ApiException(-1, "行程数据为空")
        }
        return trip
    }

    /**
     * 获取行程天气信息
     * GET {baseUrl}/api/trips/{tripId}/weather
     */
    suspend fun fetchWeather(baseUrl: String, tripId: String): List<WeatherDay> {
        val cleanBase = baseUrl.trimEnd('/')
        validateBaseUrl(cleanBase)
        val url = "$cleanBase/api/trips/${encode(tripId)}/weather"
        val json = fetch(url)
        val type = object : TypeToken<List<WeatherDay>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * 获取行李清单
     * GET {baseUrl}/api/trips/{tripId}/packing
     */
    suspend fun fetchPackingList(baseUrl: String, tripId: String): PackingListResponse {
        val cleanBase = baseUrl.trimEnd('/')
        validateBaseUrl(cleanBase)
        val url = "$cleanBase/api/trips/${encode(tripId)}/packing"
        val json = fetch(url)
        return gson.fromJson(json, PackingListResponse::class.java)
    }
}

class ApiException(val statusCode: Int, override val message: String) : Exception(message)
