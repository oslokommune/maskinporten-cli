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
import java.util.*

class MaskinportenAdminApiClient(
    private val clientsApiEndpoint: URL,
    private val authClient: JwtAuthClient,
    private val httpUtil: HttpUtil = HttpUtil()
) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(MaskinportenAdminApiClient::class.java)
        private const val MAX_KEYS = 5
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

        val token = authClient.getAccessToken(setOf("idporten:dcr.write"))

        val request = Request.Builder()
            .header("Content-Type", "application/json")
            .header("Accept", "*/*")
            .header("Authorization", "Bearer ${token.access_token}")
            .url(clientsApiEndpoint)
            .post(clientPayload(name, description, scopes).toRequestBody())
            .build()

        val response = httpUtil.post(request)

        val json = om.readTree(response)
        return json.get("client_id").textValue()
    }

    fun updateClient(clientId: String, name: String, description: String, scopes: Collection<String>): String {
        log.debug("Updating client '$clientId' with name '$name', description '$description' and scopes $scopes")

        val token = authClient.getAccessToken(setOf("idporten:dcr.write"))

        val request = Request.Builder()
            .header("Content-Type", "application/json")
            .header("Accept", "*/*")
            .header("Authorization", "Bearer ${token.access_token}")
            .url("$clientsApiEndpoint$clientId")
            .put(clientPayload(name, description, scopes).toRequestBody())
            .build()

        val response = httpUtil.post(request)

        val json = om.readTree(response)
        return json.get("client_id").textValue()
    }

    private fun clientPayload(
        name: String,
        description: String,
        scopes: Collection<String>
    ): String {
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

        return om.writeValueAsString(values)
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

    fun registerClientKey(clientId: String, publicJwk: RSAKey, overwrite: Boolean = false): String? {
        log.debug("Registering new key with id ${publicJwk.keyID} for client $clientId")
        val key = mapOf(
            "alg" to "RS256",
            "kty" to publicJwk.keyType.toString(),
            "use" to publicJwk.keyUse.toString(),
            "kid" to publicJwk.keyID,
            "e" to publicJwk.publicExponent.toString(),
            "n" to publicJwk.modulus.toString()
        )
        val keys = mutableListOf(key)

        if (!overwrite) {
            val oldKeys = om.readTree(getClientKeys(clientId)).get("keys")

            if (oldKeys != null) {
                if (oldKeys.count() >= MAX_KEYS) {
                    throw TooManyKeysError(clientId, MAX_KEYS)
                }

                for (oldKey in oldKeys) {
                    keys.add(mapOf(
                        "alg" to oldKey.get("alg").asText(),
                        "kty" to oldKey.get("kty").asText(),
                        "use" to oldKey.get("use").asText(),
                        "kid" to oldKey.get("kid").asText(),
                        "e" to oldKey.get("e").asText(),
                        "n" to oldKey.get("n").asText(),
                    ))
                }
            }
        }

        val body = ObjectMapper().writeValueAsString(mapOf("keys" to keys))
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

class TooManyKeysError(clientId: String, maxKeys: Int) :
    Exception("Client $clientId already has the maximum number of registered keys ($maxKeys)")
