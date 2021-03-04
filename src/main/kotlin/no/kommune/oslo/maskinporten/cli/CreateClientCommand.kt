package no.kommune.oslo.maskinporten.cli

import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.options.split

class CreateClientCommand : AdminCommand(name = "client") {
    private val name by option().required()
    private val description by option().required()
    private val scopes by option().split(",").default(emptyList())

    override fun run() {
        super.run()

        val clientId = getAdminClient().createClient(name, description, scopes)
        println("Client id: $clientId")
    }
}

