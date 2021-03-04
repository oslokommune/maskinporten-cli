package no.kommune.oslo.maskinporten.cli

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import no.kommune.oslo.jwt.JwtAuthClient
import java.net.URL

class TokenCommand : BaseCommand() {
    private val scope by option().required()
    private val clientId by option(envvar = "MASKINPORTEN_CLIENT_ID").required()
    private val consumerOrg by option(envvar = "MASKINPORTEN_CONSUMER_ORG").required()
    private val keyID by option(envvar = "MASKINPORTEN_KEY_ID")

    private val om = ObjectMapper().registerKotlinModule()

    override fun run() {
        super.run()

        val jwtConfig = getJwtConfig(issuer = clientId, consumerOrg, keyID)
        val wellKnownEndpoint = URL(config.getProperty("maskinporten.oidc.wellknown"))

        val jwtAuthClient = JwtAuthClient(jwtConfig, wellKnownEndpoint)
        val accessToken = jwtAuthClient.getAccessToken(setOf(scope))
        println(om.writeValueAsString(accessToken))
    }

}