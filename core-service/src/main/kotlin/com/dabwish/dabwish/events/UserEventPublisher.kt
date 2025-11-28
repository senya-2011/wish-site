package com.dabwish.dabwish.events

import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.exception.MissingCreatedAtException
import com.dabwish.dabwish.mapper.UserMapper
import com.dabwish.events.user.UserCreatedEvent
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["app.kafka.enabled"], havingValue = "true", matchIfMissing = true)
class UserEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, UserCreatedEvent>,
    private val userMapper: UserMapper,
    @Value("\${app.kafka.topics.user-created}") private val topic: String,
) {

    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    fun publishUserCreated(user: User) {
        if (user.id == 0L) {
            return
        }

        val createdAtIso = user.createdAt
            ?.format(formatter)
            ?: throw MissingCreatedAtException(user.id)

        val event = userMapper.userToUserCreatedEvent(user, createdAtIso)

        kafkaTemplate.send(topic, user.id.toString(), event)
    }
}

