package no.kommune.oslo.maskinporten.cli

import com.github.ajalt.clikt.parameters.options.*

class CreateClientCommand : AdminCommand(name = "client") {
    private val name by option().help("Name of client").required()
    private val description by option().help("Description of client").required()
    private val scopes by option()
        .help("Comma separated list of scopes")
        .split(",")
        .default(emptyList())

    override fun run() {
        super.run()

        val clientId = getAdminClient().createClient(name, description, scopes)
        println("Client id: $clientId")
    }
}

