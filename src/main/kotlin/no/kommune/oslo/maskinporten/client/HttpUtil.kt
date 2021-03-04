package no.kommune.oslo.maskinporten.client

import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class HttpUtil {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(HttpUtil::class.java)
    }

    private val client = OkHttpClient.Builder()
        .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
        .build()

    fun post(request: Request): String? {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Unexpected code $response")
            }
            return response.body?.string()
        }
    }
}