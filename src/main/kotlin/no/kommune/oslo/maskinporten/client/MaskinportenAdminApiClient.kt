package no.kommune.oslo.maskinporten.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.nimbusds.jose.jwk.RSAKey
import no.kommune.oslo.jwt.JwtAuthClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.*

class MaskinportenAdminApiClient(
    private val clientsApiEndpoint: URL,
    private val authClient: JwtAuthClient,
    private val httpUtil: HttpUtil = HttpUtil()
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(MaskinportenAdminApiClient::class.java)
    }

    private val om = ObjectMapper()

    fun getClients(): List<JsonNode> {
        log.debug("Fetching Maskinporten clients")
        val token = authClient.getAccessToken(setOf("idporten:dcr.read"))
        val request = Request.Builder()
            .header("Content-Type", "application/json")
            .header("Accept", "*/*")
            .header("Authorization", "Bearer ${token.access_token}")
            .url("$clientsApiEndpoint")
            .get()
            .build()

        val response = httpUtil.post(request)

        return om.readValue(response, Array<JsonNode>::class.java).toList()
    }

    fun getClient(clientId: String): JsonNode {
        log.debug("Fetching details for client $clientId")
        val token = authClient.getAccessToken(setOf("idporten:dcr.read"))
        val request = Request.Builder()
            .header("Content-Type", "application/json")
            .header("Accept", "*/*")
            .header("Authorization", "Bearer ${token.access_token}")
            .url("$clientsApiEndpoint$clientId")
            .get()
            .build()

        val response = httpUtil.post(request)

        return om.readTree(response)
    }

    fun createClient(name: String, description: String, scopes: Collection<String>): String {
        log.debug("Creating new client '$name' with scopes $scopes")
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

        val request = Request.Builder()
            .header("Content-Type", "application/json")
            .header("Accept", "*/*")
            .header("Authorization", "Bearer ${token.access_token}")
            .url(clientsApiEndpoint)
            .post(body.toRequestBody())
            .build()

        val response = httpUtil.post(request)

        val json = om.readTree(response)
        return json.get("client_id").textValue()
    }

    fun getClientKeys(clientId: String): String? {
        log.debug("Fetching keys for client $clientId")
        val token = authClient.getAccessToken(setOf("idporten:dcr.read"))

        val request = Request.Builder()
            .header("Content-Type", "application/json")
            .header("Accept", "*/*")
            .header("Authorization", "Bearer ${token.access_token}")
            .url("$clientsApiEndpoint$clientId/jwks")
            .get()
            .build()

        val response = httpUtil.post(request)
        return response
    }

     fun registerClientKey(clientId: String, publicJwk: RSAKey): String? {
        log.debug("Registering new key with id ${publicJwk.keyID} for client $clientId")
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