package no.kommune.oslo.maskinporten.cli

import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import no.kommune.oslo.jwt.JwtAuthClient
import no.kommune.oslo.maskinporten.client.MaskinportenAdminApiClient
import java.net.URL

abstract class AdminCommand(name: String? = null, help: String = "") : BaseCommand(name, help = help) {
    private val adminClientId by option(envvar = "MASKINPORTEN_ADMIN_CLIENT_ID")
        .help("Client id of Maskinporten admin client")
        .required()

    override fun run() {
        super.run()
        log.debug("  Admin client : $adminClientId")
    }

    fun getAdminClient(): MaskinportenAdminApiClient {
        val wellKnownEndpoint = URL(config.getProperty("idporten.oidc.wellknown"))
        val clientsApiEndpoint = URL(config.getProperty("maskinporten.clients.endpoint"))

        val jwtConfig = getJwtConfig(issuer = adminClientId)
        return MaskinportenAdminApiClient(clientsApiEndpoint, JwtAuthClient(jwtConfig, wellKnownEndpoint))
    }
}