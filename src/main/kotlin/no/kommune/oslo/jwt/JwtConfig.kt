package no.kommune.oslo.jwt

import java.security.PrivateKey
import java.security.cert.X509Certificate

data class JwtConfig(
    val issuer: String,
    val consumerOrg: String? = null,
    val keyID: String? = null,
    val certificate: X509Certificate? = null,
    val privateKey: PrivateKey,
)
