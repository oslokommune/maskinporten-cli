package no.kommune.oslo.maskinporten.cli

import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.required
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore
import java.util.*

class CreateClientKeyCommand : AdminCommand(name = "key") {
    private val clientId by option().required()
    private val out by option().default("out.p12")
    private val password by option().prompt(requireConfirmation = true, hideInput = true)

    override fun run() {
        super.run()

        val adminClient = getAdminClient()

        val client = adminClient.getClient(clientId)
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

        val clientKeyResponse = adminClient.registerClientKey(clientId, jwk)
        log.debug("Client key response: $clientKeyResponse")

        println("Wrote key with id $keyID to new keystore ${keyFile.path} with alias 'client-key'")
    }

}

