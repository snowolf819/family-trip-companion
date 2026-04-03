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
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object TripApi {

    private val gson = Gson()

    // 默认域名的证书 SHA-256 pin（主证书 + 备用证书）
    // 当证书轮换时需更新此列表
    private val PINNED_DOMAINS = mapOf(
        "plan.and.im" to setOf(
            "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",  // TODO: 替换为真实证书指纹
            "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=",  // 备用
        )
    )

    private fun sha256(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        return "sha256/" + android.util.Base64.encodeToString(md.digest(bytes), android.util.Base64.NO_WRAP)
    }

    private fun createPinnedTrustManager(pins: Set<String>): X509TrustManager {
        return object : X509TrustManager {
            @Throws(CertificateException::class)
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                // 客户端证书不校验
            }

            @Throws(CertificateException::class)
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                // 先做标准链校验（证书是否由受信 CA 签发）
                val defaultTrustManager = getDefaultTrustManager()
                defaultTrustManager.checkServerTrusted(chain, authType)

                // 再校验证书指纹
                val serverPin = sha256(chain[0].encoded)
                if (!pins.contains(serverPin)) {
                    throw CertificateException("证书指纹不匹配: $serverPin")
                }
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> =
                getDefaultTrustManager().acceptedIssuers
        }
    }

    private fun getDefaultTrustManager(): X509TrustManager {
        val factory = javax.net.ssl.TrustManagerFactory.getInstance(
            javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm()
        )
        factory.init(null as java.security.KeyStore?)
        return factory.trustManagers.first { it is X509TrustManager } as X509TrustManager
    }

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
        val parsedUrl = URL(url)
        val connection = parsedUrl.openConnection()

        // 证书固定：对已知域名启用
        if (connection is HttpsURLConnection) {
            val host = parsedUrl.host
            val pins = PINNED_DOMAINS[host]
            if (pins != null) {
                val trustManager = createPinnedTrustManager(pins)
                val sslContext = SSLContext.getInstance("TLS")
                sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
                connection.sslSocketFactory = sslContext.socketFactory
            }
        }

        if (connection is HttpURLConnection) {
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
        } else {
            throw ApiException(-1, "连接失败")
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
