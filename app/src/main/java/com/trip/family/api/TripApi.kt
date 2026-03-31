package com.trip.family.api

import com.google.gson.Gson
import com.trip.family.data.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

object TripApi {

    private val gson = Gson()

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
        // 校验 URL 格式
        try {
            URL(cleanBase)
        } catch (e: MalformedURLException) {
            throw ApiException(-1, "服务器地址格式不正确")
        }
        if (token.isBlank()) throw ApiException(-1, "分享码不能为空")

        val url = "$cleanBase/api/trips/share/$token"
        val json = fetch(url)
        return gson.fromJson(json, Trip::class.java)
    }
}

class ApiException(val statusCode: Int, override val message: String) : Exception(message)
