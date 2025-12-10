package com.dabwish.dabwish.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.cache")
data class CacheProperties(
    val ttl: Ttl = Ttl(),
) {
    data class Ttl(
        val usersSeconds: Long = 900,
        val wishesSeconds: Long = 600,
        val wishListSeconds: Long = 120,
    )
}

