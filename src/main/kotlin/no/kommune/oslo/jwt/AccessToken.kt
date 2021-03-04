package no.kommune.oslo.jwt

data class AccessToken(
    val access_token: String,
    val token_type: String,
    val expires_in: String,
    val scope: String
)