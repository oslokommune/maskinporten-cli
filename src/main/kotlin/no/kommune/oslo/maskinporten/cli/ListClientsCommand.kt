package no.kommune.oslo.maskinporten.cli

class ListClientsCommand : AdminCommand(name = "clients", help = "List existing clients") {
    override fun run() {
        super.run()

        try {
            val clients = getAdminClient().getClients()
            log.info("Found ${clients.size} clients")
            println(clients)
        } catch (ex: Exception) {
            log.error("Unable to list clients", ex)
        }
    }

}

