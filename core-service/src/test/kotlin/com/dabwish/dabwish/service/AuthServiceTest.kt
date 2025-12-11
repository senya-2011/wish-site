package com.dabwish.dabwish.service

import com.dabwish.dabwish.exception.InvalidCredentialsException
import com.dabwish.dabwish.exception.UserAlreadyExistsException
import com.dabwish.dabwish.exception.UsernameNotFoundException
import com.dabwish.dabwish.generated.dto.LoginRequest
import com.dabwish.dabwish.generated.dto.LoginResponse
import com.dabwish.dabwish.generated.dto.RegisterRequest
import com.dabwish.dabwish.mapper.AuthMapper
import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.model.user.UserRole
import com.dabwish.dabwish.repository.AuthRepository
import com.dabwish.dabwish.security.JwtTokenProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.security.crypto.password.PasswordEncoder

class AuthServiceTest : BehaviorSpec({

    isolationMode = IsolationMode.InstancePerLeaf

    val authRepository = mockk<AuthRepository>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val authMapper = mockk<AuthMapper>()
    val jwtTokenProvider = mockk<JwtTokenProvider>()

    val authService = AuthService(
        authRepository,
        passwordEncoder,
        authMapper,
        jwtTokenProvider,
    )

    Given("Login functionality") {

        When("credentials are valid") {
            val request = LoginRequest(name = "user", password = "plain")
            val user = User(id = 1, name = "user", role = UserRole.MEMBER, hashPassword = "hashed")
            val response = LoginResponse(accessToken = "jwt", expiresIn = 3600)

            every { authRepository.findByName("user") } returns user
            every { passwordEncoder.matches("plain", "hashed") } returns true
            every { jwtTokenProvider.generateToken(user) } returns "jwt"
            every { jwtTokenProvider.getExpirationSeconds() } returns 3600
            every { authMapper.toLoginResponse(user, "jwt", 3600) } returns response

            val result = authService.login(request)

            Then("it returns correct response") {
                result shouldBe response
            }

            Then("dependencies are called correctly") {
                verify {
                    authRepository.findByName("user")
                    passwordEncoder.matches("plain", "hashed")
                    jwtTokenProvider.generateToken(user)
                    authMapper.toLoginResponse(user, "jwt", 3600)
                }
            }
        }

        When("user is not found") {
            val request = LoginRequest(name = "missing", password = "plain")
            every { authRepository.findByName("missing") } returns null

            Then("it throws UsernameNotFoundException") {
                shouldThrow<UsernameNotFoundException> {
                    authService.login(request)
                }
            }
        }

        When("password is invalid") {
            val request = LoginRequest(name = "user", password = "plain")
            val user = User(id = 1, name = "user", role = UserRole.MEMBER, hashPassword = "hashed")

            every { authRepository.findByName("user") } returns user
            every { passwordEncoder.matches("plain", "hashed") } returns false

            Then("it throws InvalidCredentialsException") {
                shouldThrow<InvalidCredentialsException> {
                    authService.login(request)
                }
            }
        }
    }

    Given("Register functionality") {

        When("user is new and data is valid") {
            val request = RegisterRequest(name = "new", password = "secret1")

            val toPersist = User(name = "new", role = UserRole.MEMBER, hashPassword = "")
            val saved = User(id = 10, name = "new", role = UserRole.MEMBER, hashPassword = "encoded")

            val response = LoginResponse(accessToken = "jwt", expiresIn = 3600)

            every { authRepository.existsByName("new") } returns false
            every { authMapper.registerRequestToUser(request) } returns toPersist
            every { passwordEncoder.encode("secret1") } returns "encoded"
            every { authRepository.save(any()) } returns saved // Можно упростить до any() тут
            every { jwtTokenProvider.generateToken(saved) } returns "jwt"
            every { jwtTokenProvider.getExpirationSeconds() } returns 3600
            every { authMapper.toLoginResponse(saved, "jwt", 3600) } returns response

            val result = authService.register(request)

            Then("it returns token response") {
                result shouldBe response
            }

            Then("password was encoded and saved") {
                verify {
                    authRepository.save(withArg { candidate ->
                        candidate.hashPassword shouldBe "encoded"
                    })
                }
            }
        }

        When("user already exists") {
            val request = RegisterRequest(name = "dup", password = "secret1")
            every { authRepository.existsByName("dup") } returns true

            Then("it throws UserAlreadyExistsException") {
                shouldThrow<UserAlreadyExistsException> {
                    authService.register(request)
                }
            }
        }
    }
})