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
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.SerializationException

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties::class)
class CacheConfig(
    private val cacheProperties: CacheProperties,
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

    class FallbackJacksonRedisSerializer<T>(
        private val targetClass: Class<T>,
        private val polymorphicMapper: ObjectMapper,
        private val simpleMapper: ObjectMapper,
    ) : RedisSerializer<T> {

        override fun serialize(t: T?): ByteArray? {
            if (t == null) return null
            return try {
                polymorphicMapper.writeValueAsBytes(t)
            } catch (ex: Exception) {
                throw SerializationException("Could not serialize object of type ${t.javaClass}", ex)
            }
        }

        override fun deserialize(bytes: ByteArray?): T? {
            if (bytes == null || bytes.isEmpty()) return null

            try {
                val value = polymorphicMapper.readValue(bytes, targetClass)
                if (value != null) return value
            } catch (ignored: Exception) {

            }

            try {
                val value = simpleMapper.readValue(bytes, targetClass)
                if (value != null) return value
            } catch (ignored: Exception) {
            }

            try {
                val asMap = simpleMapper.readValue(bytes, Map::class.java) as Map<*, *>
                return simpleMapper.convertValue(asMap, targetClass)
            } catch (ex: Exception) {
                throw SerializationException("Could not deserialize bytes to ${targetClass.name}", ex)
            }
        }
    }

    @Bean
    fun redisCacheManager(
        connectionFactory: RedisConnectionFactory,
    ): RedisCacheManager {
        val polymorphicTypeValidator = BasicPolymorphicTypeValidator.builder()
            .allowIfBaseType(Any::class.java)
            .build()

        val polymorphicMapper = objectMapper.copy().apply {
            activateDefaultTyping(
                polymorphicTypeValidator,
                ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE,
                JsonTypeInfo.As.WRAPPER_ARRAY
            )
            findAndRegisterModules()
            val module = SimpleModule()
            module.addDeserializer(PageImpl::class.java, PageImplDeserializer())
            registerModule(module)
        }

        val simpleMapper = objectMapper.copy().apply {
            findAndRegisterModules()
        }

        val pageSerializer = object : RedisSerializer<Any> {
            private val generic = GenericJackson2JsonRedisSerializer(polymorphicMapper)
            private val fallback = FallbackJacksonRedisSerializer(PageImpl::class.java, polymorphicMapper, simpleMapper)

            override fun serialize(t: Any?): ByteArray? {
                if (t == null) return null
                return try {
                    generic.serialize(t)
                } catch (ex: Exception) {
                    @Suppress("UNCHECKED_CAST")
                    return (fallback as RedisSerializer<Any>).serialize(t)
                }
            }

            override fun deserialize(bytes: ByteArray?): Any? {
                if (bytes == null || bytes.isEmpty()) return null
                try {
                    val v = generic.deserialize(bytes)
                    if (v is PageImpl<*>) return v
                    if (v is Map<*, *>) {
                        val node = simpleMapper.readTree(bytes)
                        val contentNode = node.get("content")
                        val content = if (contentNode != null && contentNode.isArray) {
                            simpleMapper.convertValue(contentNode, List::class.java)
                        } else {
                            listOf<Any>()
                        }
                        val number = node.get("number")?.asInt() ?: 0
                        val size = node.get("size")?.asInt() ?: content.size
                        val totalElements = node.get("totalElements")?.asLong() ?: content.size.toLong()
                        val pageable = if (number >= 0 && size > 0) PageRequest.of(number, size) else PageRequest.of(0, content.size)
                        return PageImpl(content, pageable, totalElements)
                    }
                    if (v is PageImpl<*>) return v
                } catch (ignored: Exception) {
                }

                try {
                    return fallback.deserialize(bytes)
                } catch (ex: Exception) {
                    throw SerializationException("Could not deserialize PageImpl from cache", ex)
                }
            }
        }

        val wishClass = try {
            @Suppress("UNCHECKED_CAST")
            Class.forName("com.dabwish.dabwish.model.wish.Wish") as Class<Any>
        } catch (ex: Exception) {
            throw IllegalStateException("Could not load Wish class", ex)
        }

        @Suppress("UNCHECKED_CAST")
        val wishesByIdSerializer = FallbackJacksonRedisSerializer(wishClass as Class<Any>, polymorphicMapper, simpleMapper) as RedisSerializer<Any>

        val pagePair = RedisSerializationContext.SerializationPair.fromSerializer(pageSerializer as RedisSerializer<Any>)
        val wishPair = RedisSerializationContext.SerializationPair.fromSerializer(wishesByIdSerializer)

        val defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(pagePair)
            .entryTtl(Duration.ofSeconds(cacheProperties.ttl.wishListSeconds))

        val usersCache = defaultConfig.entryTtl(Duration.ofSeconds(cacheProperties.ttl.usersSeconds))
        val wishesCache = RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(wishPair)
            .entryTtl(Duration.ofSeconds(cacheProperties.ttl.wishesSeconds))

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
