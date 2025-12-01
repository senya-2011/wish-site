package com.dabwish.dabwish.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Duration
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
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

    class PageImplDeserializer : JsonDeserializer<PageImpl<*>>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PageImpl<*> {
            val node: JsonNode = p.codec.readTree(p)
            
            val content = ctxt.readTreeAsValue(node.get("content"), List::class.java)
            val number = node.get("number")?.asInt() ?: 0
            val size = node.get("size")?.asInt() ?: content.size
            val totalElements = node.get("totalElements")?.asLong() ?: content.size.toLong()
            
            val pageable = if (number >= 0 && size > 0) {
                PageRequest.of(number, size)
            } else {
                PageRequest.of(0, content.size)
            }
            
            return PageImpl(content, pageable, totalElements)
        }
    }

    @Bean
    fun redisCacheManager(
        connectionFactory: RedisConnectionFactory,
    ): RedisCacheManager {
        val polymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Any::class.java)
            .build()
        
        val redisObjectMapper = objectMapper.copy().apply {
            activateDefaultTyping(polymorphicTypeValidator, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY)
            findAndRegisterModules()
            
            val module = SimpleModule()
            module.addDeserializer(PageImpl::class.java, PageImplDeserializer())
            registerModule(module)
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
            .enableStatistics()
            .build()
    }
}

