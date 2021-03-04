package no.kommune.oslo.maskinporten.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nimbusds.jose.jwk.RSAKey
import no.kommune.oslo.jwt.JwtAuthClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL

class MaskinportenAdminApiClient(
    private val clientsApiEndpoint: URL,
    private val authClient: JwtAuthClient,
    private val httpUtil: HttpUtil = HttpUtil()
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(MaskinportenAdminApiClient::class.java)
    }

    private val om = ObjectMapper()

    fun getClient(clientId: String): JsonNode {
        val token = authClient.getAccessToken(setOf("idporten:dcr.read"))
        val request = Request.Builder()
            .header("Content-Type", "application/json")
            .header("Accept", "*/*")
            .header("Authorization", "Bearer ${token.access_token}")
            .url("$clientsApiEndpoint$clientId")
            .get()
            .build()

        val response = httpUtil.post(request)
        log.debug("Got response: $response")

        return om.readTree(response)
    }

    fun createClient(name: String, description: String, scopes: Collection<String>): String {
        val values = mapOf(
            "integration_type" to "maskinporten",
            "application_type" to "web",
            "client_name" to name,
            "description" to description,
            "token_endpoint_auth_method" to "private_key_jwt",
            "grant_types" to listOf(
                "urn:ietf:params:oauth:grant-type:jwt-bearer"
            ),
            "scopes" to scopes,
        )

        val body: String = om.writeValueAsString(values)

        val token = authClient.getAccessToken(setOf("idporten:dcr.write"))

        log.debug("POSTing: $body")

        val request = Request.Builder()
            .header("Content-Type", "application/json")
            .header("Accept", "*/*")
            .header("Authorization", "Bearer ${token.access_token}")
            .url(clientsApiEndpoint)
            .post(body.toRequestBody())
            .build()

        val response = httpUtil.post(request)
        log.debug("Response: $response")

        val json = om.readTree(response)
        return json.get("client_id").textValue()
    }

    fun getClientKeys(clientId: String): String? {
        val token = authClient.getAccessToken(setOf("idporten:dcr.read"))

        val request = Request.Builder()
            .header("Content-Type", "application/json")
            .header("Accept", "*/*")
            .header("Authorization", "Bearer ${token.access_token}")
            .url("$clientsApiEndpoint$clientId/jwks")
            .get()
            .build()

        return httpUtil.post(request)
    }

     fun registerClientKey(clientId: String, publicJwk: RSAKey): String? {
        val key = mapOf(
            "alg" to "RS256",
            "kty" to publicJwk.keyType.toString(),
            "use" to publicJwk.keyUse.toString(),
            "kid" to publicJwk.keyID,
            "e" to publicJwk.publicExponent.toString(),
            "n" to publicJwk.modulus.toString()
        )
        val keys = mapOf("keys" to listOf(key))
        val body = ObjectMapper().writeValueAsString(keys)

        log.debug("Keys payload: $body")

         val token = authClient.getAccessToken(setOf("idporten:dcr.write"))

         val request = Request.Builder()
            .header("Content-Type", "application/json")
            .header("Accept", "*/*")
            .header("Authorization", "Bearer ${token.access_token}")
            .url("$clientsApiEndpoint$clientId/jwks")
            .post(body.toRequestBody())
            .build()

        return httpUtil.post(request)
    }
}