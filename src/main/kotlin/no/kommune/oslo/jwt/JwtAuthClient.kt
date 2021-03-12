package no.kommune.oslo.jwt

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import okhttp3.ConnectionSpec
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL

class JwtAuthClient(jwtConfig: JwtConfig, wellKnownEndpoint: URL) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(JwtAuthClient::class.java)
    }

    private val audience: String
    private val tokenEndpoint: String

    private val jwtGenerator = JwtGenerator(jwtConfig)

    private val om = ObjectMapper().registerKotlinModule()

    private val client = OkHttpClient.Builder()
        .connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
        .build()

    init {
        val wellKnownConfig = om.readTree(wellKnownEndpoint)
        audience = wellKnownConfig.get("issuer").textValue()
        tokenEndpoint = wellKnownConfig.get("token_endpoint").textValue()
        log.debug("Maskinporten auth client:")
        log.debug("  Audience: $audience")
        log.debug("  Endpoint: $tokenEndpoint")
    }

    fun getAccessToken(scopes: Set<String>): AccessToken {
        log.debug("Getting Maskinporten token for scopes $scopes")
        val formBody = FormBody.Builder()
            .add("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
            .add("assertion", jwtGenerator.generateJwt(audience, scopes))
            .build()
        val request = Request.Builder()
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Accept", "*/*")
            .url(tokenEndpoint)
            .post(formBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val responseBody = response.body?.string() ?: "Unknown error [${response.code}]"
                throw JwtAuthException(responseBody)
            }
            try {
                val token = om.readValue(response.body?.string(), AccessToken::class.java)
                log.debug("Received Maskinporten token valid for ${token.expires_in} seconds")
                return token
            } catch (ex: Exception) {
                when (ex) {
                    is JsonProcessingException, is JsonMappingException -> {
                        log.error("Could not process response: ${ex.message}", ex)
                        throw ex
                    }
                    else -> {
                        log.error("Unknown error: ${ex.message}", ex)
                        throw ex
                    }

                }
            }
        }
    }
}

class JwtAuthException(msg: String) : Exception(msg)
