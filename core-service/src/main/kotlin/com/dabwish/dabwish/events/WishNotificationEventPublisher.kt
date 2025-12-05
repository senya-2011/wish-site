package com.dabwish.dabwish.events

import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.model.wish.Wish
import com.dabwish.events.wish.WishNotificationEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["app.kafka.enabled"], havingValue = "true", matchIfMissing = true)
class WishNotificationEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, WishNotificationEvent>,
    @Value("\${app.kafka.topics.wish-notification:wish-notification-events}") private val topic: String,
) {
    fun publishWishNotification(wish: Wish, subscriber: User) {
        val subscriberTelegramUsername = subscriber.telegramUsername
            ?: return
        
        val event = WishNotificationEvent.newBuilder()
            .setWishId(wish.id)
            .setOwnerId(wish.user.id)
            .setOwnerName(wish.user.name)
            .setWishTitle(wish.title)
            .setSubscriberId(subscriber.id)
            .setSubscriberTelegramUsername(subscriberTelegramUsername)
            .build()
        
        kafkaTemplate.send(topic, "${wish.id}-${subscriber.id}", event)
    }
}

