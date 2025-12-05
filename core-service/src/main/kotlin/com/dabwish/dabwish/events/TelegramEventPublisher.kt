package com.dabwish.dabwish.events

import com.dabwish.events.telegram.TelegramVerificationCodeEvent
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["app.kafka.enabled"], havingValue = "true", matchIfMissing = true)
class TelegramEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, TelegramVerificationCodeEvent>,
    @Value("\${app.kafka.topics.telegram-verification-code:telegram-verification-code-events}") private val topic: String,
) {
    fun publishVerificationCode(userId: Long, telegramUsername: String, verificationCode: String) {
        val event = TelegramVerificationCodeEvent.newBuilder()
            .setUserId(userId)
            .setTelegramUsername(telegramUsername)
            .setVerificationCode(verificationCode)
            .build()
        
        kafkaTemplate.send(topic, userId.toString(), event)
    }
}

