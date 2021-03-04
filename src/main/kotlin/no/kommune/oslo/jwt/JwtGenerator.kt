package no.kommune.oslo.jwt

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.util.Base64
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Clock
import java.util.*

class JwtGenerator(private val jwtConfig: JwtConfig) {
    companion object {
        private val log: Logger = LoggerFactory.getLogger(JwtGenerator::class.java)
    }

    fun generateJwt(audience: String, scopes: Set<String>): String {
        val signedJWT = SignedJWT(
            jwsHeader(),
            claims(audience, scopes)
        )
        signedJWT.sign(jwsSigner())
        return signedJWT.serialize()
    }

    private fun claims(audience: String, scopes: Set<String>): JWTClaimsSet {
        val issueTime = Clock.systemUTC().millis()
        val expirationTime = issueTime + 120_000
        var builder = JWTClaimsSet.Builder()
            .audience(audience)
            .issuer(jwtConfig.issuer)
            .claim("scope", scopes.joinToString(separator = " "))
            .jwtID(UUID.randomUUID().toString()) // Must be unique for each grant
            .issueTime(Date(issueTime)) // Use UTC time!
            .expirationTime(Date(expirationTime)) // Expiration time is 120 sec.
        if (jwtConfig.consumerOrg != null) {
            builder = builder.claim("consumer_org", jwtConfig.consumerOrg)
        }
        val claims = builder.build()
        log.debug("Creating token with claims: [{}]", claims)
        return claims
    }

    private fun jwsHeader(): JWSHeader {
        var builder = JWSHeader.Builder(JWSAlgorithm.RS256)
        if (jwtConfig.keyID != null) {
            builder = builder.keyID(jwtConfig.keyID)
        } else if (jwtConfig.certificate != null) {
            val chain = listOf(Base64.encode(jwtConfig.certificate.encoded))
            builder = builder.x509CertChain(chain) // For å legge til x5c-element i JWT
        }
        val jwsHeader = builder.build()
        log.debug("JWS header: $jwsHeader")
        return jwsHeader
    }

    private fun jwsSigner(): RSASSASigner {
        return RSASSASigner(jwtConfig.privateKey)
    }

}