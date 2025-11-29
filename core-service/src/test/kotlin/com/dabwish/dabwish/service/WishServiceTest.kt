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
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class WishServiceTest {

    private val wishRepository = mockk<WishRepository>()
    private val wishMapper = mockk<WishMapper>(relaxUnitFun = true)
    private val userRepository = mockk<UserRepository>()
    private val eventPublisher = mockk<WishEventPublisher>(relaxed = true)

    private val service = WishService(
        wishRepository,
        wishMapper,
        userRepository,
        eventPublisher
    )

    @BeforeEach
    fun setUp() {
        // Очистка моков перед каждым тестом (опционально, но хорошая практика)
    }

    @Test
    fun `findAllByUserId returns wishes`() {
        val wish = mockk<Wish>()
        every { userRepository.existsById(1L) } returns true
        every { wishRepository.findAllByUserId(1L) } returns listOf(wish)

        val result = service.findAllByUserId(1L)

        assertEquals(1, result.size)
        verify(exactly = 1) { userRepository.existsById(1L) }
        verify(exactly = 1) { wishRepository.findAllByUserId(1L) }
    }

    @Test
    fun `findAllByUserId throws if user not found`() {
        every { userRepository.existsById(1L) } returns false

        assertThrows<UserNotFoundException> { 
            service.findAllByUserId(1L) 
        }

        verify(exactly = 1) { userRepository.existsById(1L) }
        verify(exactly = 0) { wishRepository.findAllByUserId(any()) }
    }

    @Test
    fun `findById returns wish`() {
        val wish = mockk<Wish>()
        every { wishRepository.findById(10L) } returns Optional.of(wish)

        val result = service.findById(10L)

        assertEquals(wish, result)
        verify(exactly = 1) { wishRepository.findById(10L) }
    }

    @Test
    fun `findById throws if wish not found`() {
        every { wishRepository.findById(10L) } returns Optional.empty()

        assertThrows<WishNotFoundException> { 
            service.findById(10L) 
        }

        verify(exactly = 1) { wishRepository.findById(10L) }
    }

    @Test
    fun `create saves wish and publishes event`() {
        val request = mockk<WishRequest>()
        val user = mockk<User>()
        val wish = mockk<Wish>()

        every { userRepository.findById(1L) } returns Optional.of(user)
        every { wishMapper.toEntity(request, user) } returns wish
        every { wishRepository.save(wish) } returns wish

        val result = service.create(1L, request)

        assertEquals(wish, result)
        verify(exactly = 1) { userRepository.findById(1L) }
        verify(exactly = 1) { wishMapper.toEntity(request, user) }
        verify(exactly = 1) { wishRepository.save(wish) }
        verify(exactly = 1) { eventPublisher.publishWishCreated(wish) }
    }

    @Test
    fun `delete removes entity`() {
        every { wishRepository.existsById(5L) } returns true
        every { wishRepository.deleteById(5L) } just Runs

        service.delete(5L)

        verify(exactly = 1) { wishRepository.existsById(5L) }
        verify(exactly = 1) { wishRepository.deleteById(5L) }
    }

    @Test
    fun `delete throws if wish not found`() {
        every { wishRepository.existsById(5L) } returns false

        assertThrows<WishNotFoundException> { 
            service.delete(5L) 
        }

        verify(exactly = 1) { wishRepository.existsById(5L) }
        verify(exactly = 0) { wishRepository.deleteById(any()) }
    }

    @Test
    fun `update modifies wish and publishes event`() {
        val request = mockk<WishUpdateRequest>()
        val wish = mockk<Wish>()

        every { wishRepository.findById(2L) } returns Optional.of(wish)
        every { wishRepository.save(wish) } returns wish

        val result = service.update(2L, request)

        assertEquals(wish, result)
        verify(exactly = 1) { wishRepository.findById(2L) }
        verify(exactly = 1) { wishMapper.updateEntityFromRequest(request, wish) }
        verify(exactly = 1) { wishRepository.save(wish) }
        verify(exactly = 1) { eventPublisher.publishWishUpdated(wish) }
    }
}