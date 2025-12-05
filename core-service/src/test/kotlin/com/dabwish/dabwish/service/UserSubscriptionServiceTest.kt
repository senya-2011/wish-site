package com.dabwish.dabwish.service

import com.dabwish.dabwish.exception.AlreadySubscribedException
import com.dabwish.dabwish.exception.CannotSubscribeToSelfException
import com.dabwish.dabwish.exception.NotSubscribedException
import com.dabwish.dabwish.exception.UserNotFoundException
import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.model.user.UserRole
import com.dabwish.dabwish.model.user.UserSubscription
import com.dabwish.dabwish.repository.UserRepository
import com.dabwish.dabwish.repository.UserSubscriptionRepository
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

class UserSubscriptionServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val userSubscriptionRepository = mockk<UserSubscriptionRepository>()

    private val service = UserSubscriptionService(userRepository, userSubscriptionRepository)

    private val subscriber = User(id = 1L, name = "Subscriber", role = UserRole.MEMBER)
    private val subscribedTo = User(id = 2L, name = "SubscribedTo", role = UserRole.MEMBER)

    @BeforeEach
    fun setUp() {
        clearMocks(userRepository, userSubscriptionRepository)
    }

    @Test
    fun `subscribe creates subscription when valid`() {
        every { userRepository.findById(1L) } returns Optional.of(subscriber)
        every { userRepository.findById(2L) } returns Optional.of(subscribedTo)
        every { userSubscriptionRepository.existsBySubscriberIdAndSubscribedToId(1L, 2L) } returns false
        every { userSubscriptionRepository.save(any()) } answers { firstArg() }

        val result = service.subscribe(1L, 2L)

        assertNotNull(result)
        verify(exactly = 1) { userRepository.findById(1L) }
        verify(exactly = 1) { userRepository.findById(2L) }
        verify(exactly = 1) { userSubscriptionRepository.existsBySubscriberIdAndSubscribedToId(1L, 2L) }
        verify(exactly = 1) { userSubscriptionRepository.save(any()) }
    }

    @Test
    fun `subscribe throws CannotSubscribeToSelfException when subscribing to self`() {
        assertThrows<CannotSubscribeToSelfException> {
            service.subscribe(1L, 1L)
        }

        verify(exactly = 0) { userSubscriptionRepository.save(any()) }
    }

    @Test
    fun `subscribe throws UserNotFoundException when subscriber not found`() {
        every { userRepository.findById(1L) } returns Optional.empty()

        assertThrows<UserNotFoundException> {
            service.subscribe(1L, 2L)
        }

        verify(exactly = 0) { userSubscriptionRepository.save(any()) }
    }

    @Test
    fun `subscribe throws UserNotFoundException when subscribedTo not found`() {
        every { userRepository.findById(1L) } returns Optional.of(subscriber)
        every { userRepository.findById(2L) } returns Optional.empty()

        assertThrows<UserNotFoundException> {
            service.subscribe(1L, 2L)
        }

        verify(exactly = 0) { userSubscriptionRepository.save(any()) }
    }

    @Test
    fun `subscribe throws AlreadySubscribedException when already subscribed`() {
        every { userRepository.findById(1L) } returns Optional.of(subscriber)
        every { userRepository.findById(2L) } returns Optional.of(subscribedTo)
        every { userSubscriptionRepository.existsBySubscriberIdAndSubscribedToId(1L, 2L) } returns true

        assertThrows<AlreadySubscribedException> {
            service.subscribe(1L, 2L)
        }

        verify(exactly = 0) { userSubscriptionRepository.save(any()) }
    }

    @Test
    fun `unsubscribe removes subscription when exists`() {
        every { userSubscriptionRepository.existsBySubscriberIdAndSubscribedToId(1L, 2L) } returns true
        every { userSubscriptionRepository.deleteBySubscriberIdAndSubscribedToId(1L, 2L) } returns Unit

        service.unsubscribe(1L, 2L)

        verify(exactly = 1) { userSubscriptionRepository.existsBySubscriberIdAndSubscribedToId(1L, 2L) }
        verify(exactly = 1) { userSubscriptionRepository.deleteBySubscriberIdAndSubscribedToId(1L, 2L) }
    }

    @Test
    fun `unsubscribe throws NotSubscribedException when subscription not exists`() {
        every { userSubscriptionRepository.existsBySubscriberIdAndSubscribedToId(1L, 2L) } returns false

        assertThrows<NotSubscribedException> {
            service.unsubscribe(1L, 2L)
        }

        verify(exactly = 0) { userSubscriptionRepository.deleteBySubscriberIdAndSubscribedToId(any(), any()) }
    }

    @Test
    fun `getSubscribers returns list of subscribers`() {
        val subscription = UserSubscription(
            id = 1L,
            subscriber = subscriber,
            subscribedTo = subscribedTo
        )
        every { userRepository.existsById(2L) } returns true
        every { userSubscriptionRepository.findBySubscribedToId(2L) } returns listOf(subscription)

        val result = service.getSubscribers(2L)

        assertEquals(1, result.size)
        assertEquals(subscriber.id, result[0].id)
        verify(exactly = 1) { userSubscriptionRepository.findBySubscribedToId(2L) }
    }

    @Test
    fun `getSubscribers throws UserNotFoundException when user not found`() {
        every { userRepository.existsById(2L) } returns false

        assertThrows<UserNotFoundException> {
            service.getSubscribers(2L)
        }
    }

    @Test
    fun `getSubscriptions returns list of subscriptions`() {
        val subscription = UserSubscription(
            id = 1L,
            subscriber = subscriber,
            subscribedTo = subscribedTo
        )
        every { userRepository.existsById(1L) } returns true
        every { userSubscriptionRepository.findBySubscriberId(1L) } returns listOf(subscription)

        val result = service.getSubscriptions(1L)

        assertEquals(1, result.size)
        assertEquals(subscribedTo.id, result[0].id)
        verify(exactly = 1) { userSubscriptionRepository.findBySubscriberId(1L) }
    }

    @Test
    fun `getSubscriptions with pageable returns page`() {
        val subscription = UserSubscription(
            id = 1L,
            subscriber = subscriber,
            subscribedTo = subscribedTo
        )
        val pageable = PageRequest.of(0, 10)
        val subscriptionsPage = PageImpl(listOf(subscription), pageable, 1)

        every { userRepository.existsById(1L) } returns true
        every { userSubscriptionRepository.findBySubscriberId(1L, pageable) } returns subscriptionsPage

        val result = service.getSubscriptions(1L, pageable)

        assertEquals(1, result.totalElements)
        assertEquals(1, result.content.size)
        assertEquals(subscribedTo.id, result.content[0].id)
        verify(exactly = 1) { userSubscriptionRepository.findBySubscriberId(1L, pageable) }
    }

    @Test
    fun `isSubscribed returns true when subscription exists`() {
        every { userSubscriptionRepository.existsBySubscriberIdAndSubscribedToId(1L, 2L) } returns true

        val result = service.isSubscribed(1L, 2L)

        assertEquals(true, result)
        verify(exactly = 1) { userSubscriptionRepository.existsBySubscriberIdAndSubscribedToId(1L, 2L) }
    }

    @Test
    fun `isSubscribed returns false when subscription does not exist`() {
        every { userSubscriptionRepository.existsBySubscriberIdAndSubscribedToId(1L, 2L) } returns false

        val result = service.isSubscribed(1L, 2L)

        assertEquals(false, result)
        verify(exactly = 1) { userSubscriptionRepository.existsBySubscriberIdAndSubscribedToId(1L, 2L) }
    }
}

