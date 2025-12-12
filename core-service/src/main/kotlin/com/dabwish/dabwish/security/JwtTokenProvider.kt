package com.dabwish.dabwish.security

import com.dabwish.dabwish.model.user.User
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.charset.StandardCharsets
import java.util.Date
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class JwtTokenProvider(
    @Value("\${app.jwt.secret}") private val secret: String,
    @Value("\${app.jwt.expiration-seconds}") private val expirationSeconds: Long,
) {
    private val signingKey = Keys.hmacShaKeyFor(secret.toByteArray(StandardCharsets.UTF_8))
    private val parser = Jwts.parser().verifyWith(signingKey).build()

    fun generateToken(user: User): String {
        val now = Date()
        val expiry = Date(now.time + expirationSeconds * 1000)

        return Jwts.builder()
            .subject(user.id.toString())
            .claim("name", user.name)
            .claim("role", user.role.name)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact()
    }

    fun validateToken(token: String): Boolean =
        try {
            parser.parseSignedClaims(token)
            true
        } catch (ex: ExpiredJwtException) {
            false
        } catch (ex: JwtException) {
            false
        } catch (ex: IllegalArgumentException) {
            false
        }

    fun getUserId(token: String): Long? =
        try {
            parser.parseSignedClaims(token).payload.subject.toLong()
        } catch (ex: Exception) {
            logger.warn { "Failed to parse user id from token: ${ex.message}" }
            null
        }

    fun getExpirationSeconds(): Long = expirationSeconds
}

