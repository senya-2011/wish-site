package com.dabwish.dabwish.service

import com.dabwish.dabwish.exception.InvalidPasswordException
import com.dabwish.dabwish.exception.UserAlreadyExistsException
import com.dabwish.dabwish.exception.UserNotFoundException
import com.dabwish.dabwish.generated.dto.LoginRequest
import com.dabwish.dabwish.generated.dto.LoginResponse
import com.dabwish.dabwish.generated.dto.RegisterRequest
import com.dabwish.dabwish.mapper.AuthMapper
import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.model.user.UserRole
import com.dabwish.dabwish.repository.AuthRepository
import com.dabwish.dabwish.security.JwtTokenProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.password.PasswordEncoder

class AuthServiceTest {

    private val authRepository: AuthRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()
    private val authMapper: AuthMapper = mockk()
    private val jwtTokenProvider: JwtTokenProvider = mockk()

    private val authService = AuthService(
        authRepository,
        passwordEncoder,
        authMapper,
        jwtTokenProvider,
    )

    @Test
    fun `login returns response when credentials are valid`() {
        val request = LoginRequest(name = "user", password = "plain")
        val user = User(id = 1, name = "user", role = UserRole.MEMBER, hashPassword = "hashed")
        val response = LoginResponse(accessToken = "jwt", expiresIn = 3600)

        every { authRepository.findByName("user") } returns user
        every { passwordEncoder.matches("plain", "hashed") } returns true
        every { jwtTokenProvider.generateToken(user) } returns "jwt"
        every { jwtTokenProvider.getExpirationSeconds() } returns 3600
        every { authMapper.toLoginResponse(user, "jwt", 3600) } returns response

        val result = authService.login(request)

        assertEquals(response, result)
        verify {
            authRepository.findByName("user")
            passwordEncoder.matches("plain", "hashed")
            jwtTokenProvider.generateToken(user)
            authMapper.toLoginResponse(user, "jwt", 3600)
        }
    }

    @Test
    fun `login throws when user not found`() {
        val request = LoginRequest(name = "missing", password = "plain")
        every { authRepository.findByName("missing") } returns null

        assertThrows(UserNotFoundException::class.java) {
            authService.login(request)
        }
    }

    @Test
    fun `login throws when password invalid`() {
        val request = LoginRequest(name = "user", password = "plain")
        val user = User(id = 1, name = "user", role = UserRole.MEMBER, hashPassword = "hashed")

        every { authRepository.findByName("user") } returns user
        every { passwordEncoder.matches("plain", "hashed") } returns false

        assertThrows(InvalidPasswordException::class.java) {
            authService.login(request)
        }
    }

    @Test
    fun `register creates user and returns token`() {
        val request = RegisterRequest(name = "new", password = "secret1")
        val toPersist = User(name = "new", role = UserRole.MEMBER)
        val saved = toPersist.copy(id = 10, hashPassword = "encoded")
        val response = LoginResponse(accessToken = "jwt", expiresIn = 3600)

        every { authRepository.existsByName("new") } returns false
        every { authMapper.registerRequestToUser(request) } returns toPersist
        every { passwordEncoder.encode("secret1") } returns "encoded"
        every { authRepository.save(toPersist) } returns saved
        every { jwtTokenProvider.generateToken(saved) } returns "jwt"
        every { jwtTokenProvider.getExpirationSeconds() } returns 3600
        every { authMapper.toLoginResponse(saved, "jwt", 3600) } returns response

        val result = authService.register(request)

        assertEquals(response, result)
        assertEquals("encoded", toPersist.hashPassword)
        verify {
            authRepository.existsByName("new")
            authMapper.registerRequestToUser(request)
            passwordEncoder.encode("secret1")
            authRepository.save(toPersist)
            jwtTokenProvider.generateToken(saved)
            authMapper.toLoginResponse(saved, "jwt", 3600)
        }
    }

    @Test
    fun `register throws when user already exists`() {
        val request = RegisterRequest(name = "dup", password = "secret1")
        every { authRepository.existsByName("dup") } returns true

        assertThrows(UserAlreadyExistsException::class.java) {
            authService.register(request)
        }
    }
}

