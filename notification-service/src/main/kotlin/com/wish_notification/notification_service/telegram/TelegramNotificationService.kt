package com.wish_notification.notification_service.telegram

import com.dabwish.events.wish.WishNotificationEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage

@Service
class TelegramNotificationService(
    private val bot: TelegramBot,
    private val chatRegistry: TelegramChatRegistry,
) {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun sendWishNotification(event: WishNotificationEvent) {
        val username = event.subscriberTelegramUsername
        val chatId = chatRegistry.getChatId(username)

        if (chatId == null) {
            log.info(
                "Skip wish notification for @{}: chatId not registered yet (user has not sent /start)",
                username
            )
            return
        }

        val text = "${event.ownerName} создал новое желание: \"${event.wishTitle}\""

        try {
            val message = SendMessage(chatId.toString(), text)
            bot.execute(message)
            log.info(
                "Sent wish notification to chatId {} for @{} (wishId={})",
                chatId,
                username,
                event.wishId
            )
        } catch (e: Exception) {
            log.warn(
                "Failed to send wish notification to chatId {} for @{}: {}",
                chatId,
                username,
                e.message,
                e
            )
        }
    }
}


