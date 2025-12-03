package com.dabwish.dabwish.service

import com.dabwish.dabwish.exception.AlreadySubscribedException
import com.dabwish.dabwish.exception.CannotSubscribeToSelfException
import com.dabwish.dabwish.exception.NotSubscribedException
import com.dabwish.dabwish.exception.UserNotFoundException
import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.model.user.UserSubscription
import com.dabwish.dabwish.repository.UserRepository
import com.dabwish.dabwish.repository.UserSubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserSubscriptionService(
    private val userRepository: UserRepository,
    private val userSubscriptionRepository: UserSubscriptionRepository,
) {
    @Transactional
    fun subscribe(subscriberId: Long, subscribedToId: Long): UserSubscription {
        if (subscriberId == subscribedToId) {
            throw CannotSubscribeToSelfException()
        }

        val subscriber = userRepository.findById(subscriberId)
            .orElseThrow { UserNotFoundException(subscriberId) }
        val subscribedTo = userRepository.findById(subscribedToId)
            .orElseThrow { UserNotFoundException(subscribedToId) }

        if (userSubscriptionRepository.existsBySubscriberIdAndSubscribedToId(subscriberId, subscribedToId)) {
            throw AlreadySubscribedException(subscriberId, subscribedToId)
        }

        val subscription = UserSubscription(
            subscriber = subscriber,
            subscribedTo = subscribedTo,
        )
        return userSubscriptionRepository.save(subscription)
    }

    @Transactional
    fun unsubscribe(subscriberId: Long, subscribedToId: Long) {
        if (!userSubscriptionRepository.existsBySubscriberIdAndSubscribedToId(subscriberId, subscribedToId)) {
            throw NotSubscribedException(subscriberId, subscribedToId)
        }

        userSubscriptionRepository.deleteBySubscriberIdAndSubscribedToId(subscriberId, subscribedToId)
    }

    fun getSubscribers(userId: Long): List<User> {
        if (!userRepository.existsById(userId)) {
            throw UserNotFoundException(userId)
        }

        val subscriptions = userSubscriptionRepository.findBySubscribedToId(userId)
        return subscriptions.map { it.subscriber }
    }

    fun getSubscriptions(userId: Long): List<User> {
        if (!userRepository.existsById(userId)) {
            throw UserNotFoundException(userId)
        }

        val subscriptions = userSubscriptionRepository.findBySubscriberId(userId)
        return subscriptions.map { it.subscribedTo }
    }

    fun isSubscribed(subscriberId: Long, subscribedToId: Long): Boolean {
        return userSubscriptionRepository.existsBySubscriberIdAndSubscribedToId(subscriberId, subscribedToId)
    }
}

