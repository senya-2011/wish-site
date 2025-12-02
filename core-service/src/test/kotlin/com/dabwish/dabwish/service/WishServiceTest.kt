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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

class WishServiceTest {

    private val wishRepository = mockk<WishRepository>()
    private val wishMapper = mockk<WishMapper>(relaxUnitFun = true)
    private val userRepository = mockk<UserRepository>()
    private val eventPublisher = mockk<WishEventPublisher>(relaxed = true)
    private val minioService = mockk<MinioService>()

    private val service = WishService(
        wishRepository,
        wishMapper,
        userRepository,
        eventPublisher,
        minioService
    )

    @BeforeEach
    fun setUp() {
        // Очистка моков перед каждым тестом (опционально, но хорошая практика)
    }

    @Test
    fun `findAllByUserId returns page of wishes`() {
        val wish = mockk<Wish>()
        val pageable = PageRequest.of(0, 10)
        every { userRepository.existsById(1L) } returns true
        every { wishRepository.findAllByUserId(1L, pageable) } returns PageImpl(listOf(wish), pageable, 1)

        val result = service.findAllByUserId(1L, pageable)

        assertEquals(1, result.totalElements)
        verify(exactly = 1) { userRepository.existsById(1L) }
        verify(exactly = 1) { wishRepository.findAllByUserId(1L, pageable) }
    }

    @Test
    fun `findAllByUserId throws if user not found`() {
        every { userRepository.existsById(1L) } returns false

        assertThrows<UserNotFoundException> { 
            service.findAllByUserId(1L, PageRequest.of(0, 10)) 
        }

        verify(exactly = 1) { userRepository.existsById(1L) }
        verify(exactly = 0) { wishRepository.findAllByUserId(any(), any()) }
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
        val wish = mockk<Wish>()
        every { wish.photoUrl } returns "http://some-url/photo.jpg"
        every { minioService.extractObjectNameFromUrl("http://some-url/photo.jpg") } returns "photo.jpg"
        every { minioService.deleteFile("photo.jpg") } returns Unit
        every { wishRepository.findById(5L) } returns Optional.of(wish)
        every { wishRepository.deleteById(5L) } just Runs
        every { wishRepository.countByPhotoUrl("http://some-url/photo.jpg")} returns 1L

        service.delete(5L)

        verify(exactly = 1) { wishRepository.findById(5L) }
        verify(exactly = 1) { wishRepository.deleteById(5L) }
        verify(exactly = 1) { minioService.deleteFile("photo.jpg") }
    }

    @Test
    fun `delete throws if wish not found`() {
        every { wishRepository.findById(5L) } returns Optional.empty()

        assertThrows<WishNotFoundException> {
            service.delete(5L)
        }

        verify(exactly = 1) { wishRepository.findById(5L) }
        verify(exactly = 0) { wishRepository.deleteById(any()) }
    }

    @Test
    fun `update modifies wish and publishes event`() {
        val request = mockk<WishUpdateRequest>(relaxed = true)
        val testUrl = "http://some-url/photo.jpg"
        val testObjectName = "photo.jpg"

        val wish = mockk<Wish>(relaxed = true) {
            every { photoUrl } returns testUrl
        }

        every { wishRepository.findById(2L) } returns Optional.of(wish)
        every { wishRepository.save(wish) } returns wish
        every { wishRepository.countByPhotoUrl(testUrl) } returns 1L
        every { minioService.extractObjectNameFromUrl(testUrl) } returns testObjectName
        every { minioService.deleteFile(testObjectName) } just Runs

        val result = service.update(2L, request)

        assertEquals(wish, result)
        verify(exactly = 1) { wishRepository.findById(2L) }
        verify(exactly = 1) { wishMapper.updateEntityFromRequest(request, wish) }
        verify(exactly = 1) { wishRepository.save(wish) }
        verify(exactly = 1) { eventPublisher.publishWishUpdated(wish) }
        
        verify(exactly = 1) { minioService.deleteFile(testObjectName) }
    }
}