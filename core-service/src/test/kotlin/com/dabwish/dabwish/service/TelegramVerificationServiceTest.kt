package com.dabwish.dabwish.service

import com.dabwish.dabwish.events.TelegramEventPublisher
import com.dabwish.dabwish.exception.TelegramAlreadyVerifiedException
import com.dabwish.dabwish.exception.TelegramVerificationCodeExpiredException
import com.dabwish.dabwish.exception.TelegramVerificationCodeInvalidException
import com.dabwish.dabwish.exception.UserNotFoundException
import com.dabwish.dabwish.model.user.TelegramVerificationCode
import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.model.user.UserRole
import com.dabwish.dabwish.repository.TelegramVerificationCodeRepository
import com.dabwish.dabwish.repository.UserRepository
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import java.util.*

class TelegramVerificationServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val telegramVerificationCodeRepository = mockk<TelegramVerificationCodeRepository>()
    private val telegramEventPublisher = mockk<TelegramEventPublisher>(relaxed = true)

    private val service = TelegramVerificationService(
        userRepository,
        telegramVerificationCodeRepository,
        telegramEventPublisher
    )

    private val user = User(id = 1L, name = "TestUser", role = UserRole.MEMBER, telegramUsername = null)

    @BeforeEach
    fun setUp() {
        clearMocks(userRepository, telegramVerificationCodeRepository, telegramEventPublisher)
    }

    @Test
    fun `requestVerification generates code and saves it`() {
        every { userRepository.findById(1L) } returns Optional.of(user)
        every { telegramVerificationCodeRepository.findByUserId(1L) } returns null
        every { telegramVerificationCodeRepository.save(any()) } answers { firstArg() }
        every { telegramEventPublisher.publishVerificationCode(any(), any(), any()) } returns Unit

        val code = service.requestVerification(1L, "testuser")

        assertNotNull(code)
        assertEquals(6, code.length)
        verify(exactly = 1) { telegramVerificationCodeRepository.save(any()) }
        verify(exactly = 1) { telegramEventPublisher.publishVerificationCode(1L, "testuser", code) }
    }

    @Test
    fun `requestVerification normalizes username by removing @`() {
        every { userRepository.findById(1L) } returns Optional.of(user)
        every { telegramVerificationCodeRepository.findByUserId(1L) } returns null
        every { telegramVerificationCodeRepository.save(any()) } answers { firstArg() }
        every { telegramEventPublisher.publishVerificationCode(any(), any(), any()) } returns Unit

        service.requestVerification(1L, "@testuser")

        verify(exactly = 1) { 
            telegramEventPublisher.publishVerificationCode(1L, "testuser", any())
        }
    }

    @Test
    fun `requestVerification throws TelegramAlreadyVerifiedException when already verified`() {
        val verifiedUser = user.copy(telegramUsername = "existing")
        every { userRepository.findById(1L) } returns Optional.of(verifiedUser)

        assertThrows<TelegramAlreadyVerifiedException> {
            service.requestVerification(1L, "testuser")
        }

        verify(exactly = 0) { telegramVerificationCodeRepository.save(any()) }
    }

    @Test
    fun `requestVerification throws UserNotFoundException when user not found`() {
        every { userRepository.findById(1L) } returns Optional.empty()

        assertThrows<UserNotFoundException> {
            service.requestVerification(1L, "testuser")
        }

        verify(exactly = 0) { telegramVerificationCodeRepository.save(any()) }
    }

    @Test
    fun `confirmVerification binds telegram username when code is valid`() {
        val code = "123456"
        val expiresAt = OffsetDateTime.now().plusMinutes(10)
        val verificationCode = TelegramVerificationCode(
            id = 1L,
            user = user,
            telegramUsername = "testuser",
            verificationCode = code,
            expiresAt = expiresAt
        )

        every { userRepository.findById(1L) } returns Optional.of(user)
        every { telegramVerificationCodeRepository.findByVerificationCode(code) } returns verificationCode
        every { userRepository.save(any()) } answers { 
            val savedUser = firstArg() as User
            assertEquals("testuser", savedUser.telegramUsername)
            savedUser
        }
        every { telegramVerificationCodeRepository.deleteByUserId(1L) } returns Unit

        val result = service.confirmVerification(1L, code)

        assertEquals(true, result)
        verify(exactly = 1) { userRepository.save(user) }
        verify(exactly = 1) { telegramVerificationCodeRepository.deleteByUserId(1L) }
    }

    @Test
    fun `confirmVerification throws TelegramVerificationCodeInvalidException when code not found`() {
        every { userRepository.findById(1L) } returns Optional.of(user)
        every { telegramVerificationCodeRepository.findByVerificationCode("invalid") } returns null

        assertThrows<TelegramVerificationCodeInvalidException> {
            service.confirmVerification(1L, "invalid")
        }

        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `confirmVerification throws TelegramVerificationCodeInvalidException when code belongs to different user`() {
        val otherUser = User(id = 2L, name = "Other", role = UserRole.MEMBER)
        val code = "123456"
        val verificationCode = TelegramVerificationCode(
            id = 1L,
            user = otherUser,
            telegramUsername = "testuser",
            verificationCode = code,
            expiresAt = OffsetDateTime.now().plusMinutes(10)
        )

        every { userRepository.findById(1L) } returns Optional.of(user)
        every { telegramVerificationCodeRepository.findByVerificationCode(code) } returns verificationCode

        assertThrows<TelegramVerificationCodeInvalidException> {
            service.confirmVerification(1L, code)
        }

        verify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `confirmVerification throws TelegramVerificationCodeExpiredException when code expired`() {
        val code = "123456"
        val expiredAt = OffsetDateTime.now().minusMinutes(1)
        val verificationCode = TelegramVerificationCode(
            id = 1L,
            user = user,
            telegramUsername = "testuser",
            verificationCode = code,
            expiresAt = expiredAt
        )

        every { userRepository.findById(1L) } returns Optional.of(user)
        every { telegramVerificationCodeRepository.findByVerificationCode(code) } returns verificationCode
        every { telegramVerificationCodeRepository.deleteByUserId(1L) } returns Unit

        assertThrows<TelegramVerificationCodeExpiredException> {
            service.confirmVerification(1L, code)
        }

        verify(exactly = 0) { userRepository.save(any()) }
        verify(exactly = 1) { telegramVerificationCodeRepository.deleteByUserId(1L) }
    }
}

