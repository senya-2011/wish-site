package com.dabwish.dabwish.service

import com.dabwish.dabwish.events.WishEventPublisher
import com.dabwish.dabwish.exception.UserNotFoundException
import com.dabwish.dabwish.exception.WishNotFoundException
import com.dabwish.dabwish.generated.dto.WishRequest
import com.dabwish.dabwish.generated.dto.WishUpdateRequest
import com.dabwish.dabwish.mapper.WishMapper
import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.model.wish.Wish
import com.dabwish.dabwish.repository.UserRepository
import com.dabwish.dabwish.repository.WishRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever


class WishServiceTest {

    private val wishRepository: WishRepository = mock()
    private val wishMapper: WishMapper = mock()
    private val userRepository: UserRepository = mock()
    private val eventPublisher: WishEventPublisher = mock()

    private val service = WishService(
        wishRepository,
        wishMapper,
        userRepository,
        eventPublisher
    )

    @Test
    fun `findAllByUserId returns wishes`() {
        whenever(userRepository.existsById(1L)).thenReturn(true)
        whenever(wishRepository.findAllByUserId(1L))
            .thenReturn(listOf(mock<Wish>()))

        val result = service.findAllByUserId(1L)

        assertEquals(1, result.size)
    }

    @Test
    fun `findAllByUserId throws if user not found`() {
        whenever(userRepository.existsById(1L)).thenReturn(false)
        assertThrows<UserNotFoundException> { service.findAllByUserId(1L) }
    }

    @Test
    fun `findById returns wish`() {
        val wish = mock<Wish>()
        whenever(wishRepository.findById(10L)).thenReturn(java.util.Optional.of(wish))

        assertEquals(wish, service.findById(10L))
    }

    @Test
    fun `findById throws if wish not found`() {
        whenever(wishRepository.findById(10L)).thenReturn(java.util.Optional.empty())
        assertThrows<WishNotFoundException> { service.findById(10L) }
    }

    @Test
    fun `create saves wish and publishes event`() {
        val request = mock<WishRequest>()
        val user = mock<User>()
        val wish = mock<Wish>()

        whenever(userRepository.findById(1L)).thenReturn(java.util.Optional.of(user))
        whenever(wishMapper.toEntity(request, user)).thenReturn(wish)
        whenever(wishRepository.save(wish)).thenReturn(wish)

        val result = service.create(1L, request)

        assertEquals(wish, result)
        verify(eventPublisher).publishWishCreated(wish)
    }

    @Test
    fun `delete removes entity`() {
        whenever(wishRepository.existsById(5L)).thenReturn(true)

        service.delete(5L)

        verify(wishRepository).deleteById(5L)
    }

    @Test
    fun `delete throws if wish not found`() {
        whenever(wishRepository.existsById(5L)).thenReturn(false)
        assertThrows<WishNotFoundException> { service.delete(5L) }
    }

    @Test
    fun `update modifies wish and publishes event`() {
        val request = mock<WishUpdateRequest>()
        val wish = mock<Wish>()

        whenever(wishRepository.findById(2L)).thenReturn(java.util.Optional.of(wish))
        whenever(wishRepository.save(wish)).thenReturn(wish)

        val result = service.update(2L, request)

        verify(wishMapper).updateEntityFromRequest(request, wish)
        verify(eventPublisher).publishWishUpdated(wish)
        assertEquals(wish, result)
    }
}