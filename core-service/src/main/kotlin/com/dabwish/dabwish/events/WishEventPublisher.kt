package com.dabwish.dabwish.events

import com.dabwish.dabwish.mapper.WishMapper
import com.dabwish.dabwish.model.wish.Wish
import com.dabwish.events.wish.WishCreatedEvent
import com.dabwish.events.wish.WishUpdatedEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

@Component
@ConditionalOnProperty(value = ["app.kafka.enabled"], havingValue = "true", matchIfMissing = true)
class WishEventPublisher(
    private val wishMapper: WishMapper,
    private val createdTemplate: KafkaTemplate<String, WishCreatedEvent>,
    private val updatedTemplate: KafkaTemplate<String, WishUpdatedEvent>,
    @Value("\${app.kafka.topics.wish-created}") private val createdTopic: String,
    @Value("\${app.kafka.topics.wish-updated}") private val updatedTopic: String,
) {
    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    fun publishWishCreated(wish: Wish) {
        val createdAtIso = wish.createdAt.format(formatter)
        val event = wishMapper.toCreatedEvent(wish, createdAtIso)
        createdTemplate.send(createdTopic, wish.id.toString(), event)
    }

    fun publishWishUpdated(wish: Wish) {
        val updatedAtIso = (wish.updatedAt ?: OffsetDateTime.now()).format(formatter)
        val event = wishMapper.toUpdatedEvent(wish, updatedAtIso)
        updatedTemplate.send(updatedTopic, wish.id.toString(), event)
    }
}