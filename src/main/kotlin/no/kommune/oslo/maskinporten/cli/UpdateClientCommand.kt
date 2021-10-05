package no.kommune.oslo.maskinporten.cli

import com.github.ajalt.clikt.parameters.options.*

class UpdateClientCommand : AdminCommand(name = "client", help = "Update a client in Maskinporten") {
    private val clientId by option().help("Id of client").required()
    private val name by option().help("Name of client").required()
    private val description by option().help("Description of client").required()
    private val scopes by option()
        .help("Comma separated list of scopes")
        .split(",")
        .default(emptyList())

    override fun run() {
        super.run()

        try {
            val clientId = getAdminClient().updateClient(clientId, name, description, scopes)
            log.info("Updated Maskinporten client with id $clientId")
        } catch (ex: Exception) {
            log.error("Unable to update Maskinporten client", ex)
        }
    }
}

