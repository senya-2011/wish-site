package com.dabwish.dabwish.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import java.time.Duration
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext

@Configuration
@EnableCaching
class CacheConfig(
    @Value("\${app.cache.ttl.users-seconds:900}") private val usersTtlSeconds: Long,
    @Value("\${app.cache.ttl.wishes-seconds:600}") private val wishesTtlSeconds: Long,
    @Value("\${app.cache.ttl.wish-list-seconds:120}") private val wishListTtlSeconds: Long,
    private val objectMapper: ObjectMapper,
) {

    @Bean
    fun redisCacheManager(
        connectionFactory: RedisConnectionFactory,
    ): RedisCacheManager {
        val polymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Any::class.java)
            .build()
        val redisObjectMapper = objectMapper.copy().apply {
            activateDefaultTyping(polymorphicTypeValidator, ObjectMapper.DefaultTyping.EVERYTHING, JsonTypeInfo.As.PROPERTY)
        }
        val valueSerializer = GenericJackson2JsonRedisSerializer(redisObjectMapper)
        val serializationPair = RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer)

        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(serializationPair)
            .entryTtl(Duration.ofSeconds(wishListTtlSeconds))

        val usersCache = defaultConfig.entryTtl(Duration.ofSeconds(usersTtlSeconds))
        val wishesCache = defaultConfig.entryTtl(Duration.ofSeconds(wishesTtlSeconds))

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(
                mapOf(
                    "usersById" to usersCache,
                    "wishesById" to wishesCache,
                    "userWishes" to defaultConfig,
                ),
            )
            // Enable statistics collection so that Spring Boot Actuator can expose
            // cache_* metrics (cache_gets_total, cache_puts_total, etc.)
            .enableStatistics()
            .build()
    }
}

