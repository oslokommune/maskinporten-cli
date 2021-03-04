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
    }

    fun getAccessToken(scopes: Set<String>): AccessToken {
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

        log.debug("Henter token fra maskinporten..")
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val resp = response.body?.string()
                log.warn("Feilet mot maskinporten [{}]: {}", response.code, resp)
                throw JwtAuthException("Unexpected code $response")
            }
            try {
                return om.readValue(response.body?.string(), AccessToken::class.java)
            } catch (ex: Exception) {
                when (ex) {
                    is JsonProcessingException, is JsonMappingException -> {
                        log.error("Kunne ikke prosessere response {}: {}", ex.message, ex.stackTrace)
                        throw ex
                    }
                    else -> {
                        log.error("Ukjent feil {}: {}", ex.message, ex.stackTrace)
                        throw ex
                    }

                }
            }
        }
    }
}

class JwtAuthException(msg: String) : Exception(msg)
