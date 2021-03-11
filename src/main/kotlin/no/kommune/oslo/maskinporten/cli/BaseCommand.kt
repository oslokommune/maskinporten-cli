package no.kommune.oslo.maskinporten.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import no.kommune.oslo.jwt.JwtConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate
import java.util.*

abstract class BaseCommand(name: String? = null) : CliktCommand(name = name) {
    val env by option(envvar = "MASKINPORTEN_ENV")
        .help("Maskinporten environment to use (dev / prod)")
        .default("dev")
        .check("Environment must be 'dev' or 'prod'") { it in setOf("dev", "prod") }

    private val keystorePath by option(envvar = "MASKINPORTEN_KEYSTORE_PATH")
        .help("Path to PKCS12 keystore file containing client key or certificate ('virksomhetssertifikat')")
        .required()
    private val keystorePassword by option(envvar = "MASKINPORTEN_KEYSTORE_PASSWORD")
        .help("Password to unlock PKCS12 keystore")
        .prompt(hideInput = true)
    private val keyAlias by option(envvar = "MASKINPORTEN_KEY_ALIAS")
        .help("Alias/name of client key in PKCS12 keystore")
        .required()
    private val keyPassword by option(envvar = "MASKINPORTEN_KEY_PASSWORD")
        .help("Password to unlock client key")
        .prompt(hideInput = true)

    private lateinit var keystore: KeyStore

    val log: Logger = LoggerFactory.getLogger(javaClass)

    val config = Properties()

    override fun run() {
        val keystoreFile = File(keystorePath)
        val keystoreType = if (keystoreFile.extension == "jks") "jks" else "pkcs12"
        keystore = KeyStore.getInstance(keystoreType)
        keystore.load(FileInputStream(keystoreFile), keystorePassword.toCharArray())
        config.load(javaClass.classLoader.getResourceAsStream("$env.properties"))

        log.debug("Configuration:")
        log.debug("  Environment  : $env")
        log.debug("  Keystore path: ${keystoreFile.absolutePath}")
        log.debug("  Keystore type: ${keystore.type}")
        log.debug("  Key alias    : $keyAlias")

        val certificate = certificate()
        if (certificate != null) {
            log.debug("  Certificate  : ${certificate.subjectDN}")
        }
    }

    fun getJwtConfig(issuer: String, consumerOrg: String? = null, keyID: String? = null) = JwtConfig(
        issuer = issuer,
        certificate = certificate(),
        consumerOrg = consumerOrg,
        keyID = keyID,
        privateKey = privateKey()
    )

    fun certificate(): X509Certificate? = keystore.getCertificate(keyAlias) as X509Certificate?

    private fun privateKey(): PrivateKey = keystore.getKey(keyAlias, keyPassword.toCharArray()) as PrivateKey

}
