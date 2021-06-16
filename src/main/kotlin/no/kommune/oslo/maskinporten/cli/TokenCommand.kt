package no.kommune.oslo.maskinporten.cli

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import no.kommune.oslo.jwt.JwtAuthClient
import java.net.URL

class TokenCommand() : BaseCommand(help = "Generate a token in Maskinporten") {
    private val scope by option().help("Scope to generate token for").required()
    private val clientId by option(envvar = "MASKINPORTEN_CLIENT_ID")
        .help("Id of Maskinporten client to generate token for")
        .required()
    private val consumerOrg by option(envvar = "MASKINPORTEN_CONSUMER_ORG")
        .help("Organization number of the API consumer")
        .required()
    private val keyID by option(envvar = "MASKINPORTEN_KEY_ID")
        .help("Key ID to use for Maskinporten client")

    private val om = ObjectMapper().registerKotlinModule()

    override fun run() {
        super.run()

        log.debug("  Consumer org : $consumerOrg")
        log.debug("  Client id    : $clientId")
        if (keyID != null) {
            log.debug("  Key id       : $keyID")
        }

        val jwtConfig = getJwtConfig(issuer = clientId, consumerOrg, keyID)
        val wellKnownEndpoint = URL(config.getProperty("maskinporten.oidc.wellknown"))

        try {
            val jwtAuthClient = JwtAuthClient(jwtConfig, wellKnownEndpoint)
            val accessToken = jwtAuthClient.getAccessToken(setOf(scope))
            println(om.writeValueAsString(accessToken))
        } catch (ex: Exception) {
            log.error("Unable to generate Maskinporten token", ex)
        }
    }

}