package com.dabwish.dabwish.security

import com.dabwish.dabwish.model.user.User
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.nio.charset.StandardCharsets
import java.util.Date
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

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
            .signWith(signingKey, SignatureAlgorithm.HS256)
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
        runCatching { parser.parseSignedClaims(token).payload.subject.toLong() }.getOrNull()

    fun getExpirationSeconds(): Long = expirationSeconds
}

