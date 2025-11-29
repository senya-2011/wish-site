package com.dabwish.dabwish.security

import com.dabwish.dabwish.model.user.User
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

    fun getExpirationSeconds(): Long = expirationSeconds
}

