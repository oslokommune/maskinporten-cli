package no.kommune.oslo.maskinporten.cli

import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import no.kommune.oslo.maskinporten.client.NotFoundError

class ListClientKeysCommand : AdminCommand(name = "keys") {
    private val clientId by option().help("Id of Maskinporten client to list keys for").required()

    override fun run() {
        super.run()

        try {
            val clientKeys = getAdminClient().getClientKeys(clientId)
            println(clientKeys)
        } catch (ex: Exception) {
            when (ex) {
                is NotFoundError -> log.info("No client with id $clientId")
                else -> log.error("Unable to list client keys", ex)
            }
        }
    }

}

