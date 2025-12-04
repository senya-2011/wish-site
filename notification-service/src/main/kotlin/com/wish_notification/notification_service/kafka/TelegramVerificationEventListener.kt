package com.wish_notification.notification_service.kafka

import com.dabwish.events.telegram.TelegramVerificationCodeEvent
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["app.kafka.enabled"], havingValue = "true", matchIfMissing = true)
class TelegramVerificationEventListener {
    private val log = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(
        topics = ["\${app.kafka.topics.telegram-verification-code:telegram-verification-code-events}"],
    )
    fun handleTelegramVerificationCode(event: TelegramVerificationCodeEvent) {
        log.info(
            "Код верификации для @{}: {} [User ID: {}]",
            event.telegramUsername,
            event.verificationCode,
            event.userId
        )
    }
}

