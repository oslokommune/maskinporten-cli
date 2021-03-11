package no.kommune.oslo.maskinporten.cli

import com.github.ajalt.clikt.parameters.options.*
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import no.kommune.oslo.maskinporten.client.NotFoundError
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore
import java.util.*

class CreateClientKeyCommand : AdminCommand(name = "key") {
    private val clientId by option().help("Id of Maskinporten client to create new key for").required()
    private val out by option().help("Name of client key output file").default("out.p12")
    private val password by option()
        .help("Password to set for new client key")
        .prompt(requireConfirmation = true, hideInput = true)

    override fun run() {
        super.run()

        val adminClient = getAdminClient()

        val client = try {
            adminClient.getClient(clientId)
        } catch (ex: NotFoundError) {
            log.info("No client with id $clientId")
            return
        }
        val clientName = client.get("client_name").textValue()

        val keyID = clientName + "-" + UUID.randomUUID()
        val jwk = RSAKeyGenerator(4096)
            .keyUse(KeyUse.SIGNATURE)
            .keyID(keyID)
            .generate()

        val chain = arrayOf(certificate())
        val keyStore = KeyStore.getInstance("pkcs12")
        keyStore.load(null, null) // Initialize keystore
        keyStore.setKeyEntry("client-key", jwk.toRSAPrivateKey(), password.toCharArray(), chain)

        val keyFile = File(out)
        val fos = FileOutputStream(keyFile)
        keyStore.store(fos, password.toCharArray())
        fos.close()

        try {
            adminClient.registerClientKey(clientId, jwk)

            log.info("Registered new key with id $keyID for client $clientId")
            log.info("Wrote key to keystore at ${keyFile.path} with alias 'client-key'")
            println("""{"keystore_path": "${keyFile.path}", "key_id": "$keyID", "key_alias": "client-key", "client_id": "$clientId"}""")
        } catch (ex: Exception) {
            when (ex) {
                is NotFoundError -> log.info("No client with id $clientId")
                else -> log.error("Unable to create client key", ex)
            }
        }
    }

}

