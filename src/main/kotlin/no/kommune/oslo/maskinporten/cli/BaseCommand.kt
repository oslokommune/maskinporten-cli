package no.kommune.oslo.maskinporten.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.required
import no.kommune.oslo.jwt.JwtConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.*

abstract class BaseCommand(name: String? = null) : CliktCommand(name = name) {
    val env by option(envvar = "MASKINPORTEN_ENV").default("dev")

    private val keystorePath by option(envvar = "MASKINPORTEN_KEYSTORE_PATH").required()
    private val keystorePassword by option(envvar = "MASKINPORTEN_KEYSTORE_PASSWORD").prompt(hideInput = true)
    private val keyAlias by option(envvar = "MASKINPORTEN_KEY_ALIAS").required()
    private val keyPassword by option(envvar = "MASKINPORTEN_KEY_PASSWORD").prompt(hideInput = true)

    private val keystore: KeyStore = KeyStore.getInstance("pkcs12")

    val log: Logger = LoggerFactory.getLogger(javaClass)

    val config = Properties()

    override fun run() {
        keystore.load(FileInputStream(keystorePath), keystorePassword.toCharArray())
        config.load(javaClass.classLoader.getResourceAsStream("$env.properties"))
    }

    fun getJwtConfig(issuer: String, consumerOrg: String? = null, keyID: String? = null) = JwtConfig(
        issuer = issuer,
        certificate = certificate(),
        consumerOrg = consumerOrg,
        keyID = keyID,
        privateKey = privateKey()
    )

    fun certificate(): X509Certificate = keystore.getCertificate(keyAlias) as X509Certificate

    private fun privateKey(): PrivateKey = keystore.getKey(keyAlias, keyPassword.toCharArray()) as PrivateKey

}
