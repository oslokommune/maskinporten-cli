package no.kommune.oslo.maskinporten.cli

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.subcommands

class MaskinportenCommand : NoOpCliktCommand()

class CreateCommand : NoOpCliktCommand(help = "Creates a client or keys in Maskinporten")

class ListCommand : NoOpCliktCommand(help = "List client keys from Maskinporten")

fun main(args: Array<String>) {
    MaskinportenCommand().subcommands(
        CreateCommand().subcommands(
            CreateClientCommand(),
            CreateClientKeyCommand()
        ),
        ListCommand().subcommands(
            ListClientsCommand(),
            ListClientKeysCommand()
        ),
        TokenCommand()
    ).main(args)
}
