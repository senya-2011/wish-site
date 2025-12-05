package com.dabwish.dabwish.repository

import com.dabwish.dabwish.model.user.UserSubscription
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserSubscriptionRepository : JpaRepository<UserSubscription, Long> {
    fun findBySubscriberId(subscriberId: Long): List<UserSubscription>
    fun findBySubscriberId(subscriberId: Long, pageable: Pageable): Page<UserSubscription>
    fun findBySubscribedToId(subscribedToId: Long): List<UserSubscription>
    fun existsBySubscriberIdAndSubscribedToId(subscriberId: Long, subscribedToId: Long): Boolean
    fun deleteBySubscriberIdAndSubscribedToId(subscriberId: Long, subscribedToId: Long)
}

