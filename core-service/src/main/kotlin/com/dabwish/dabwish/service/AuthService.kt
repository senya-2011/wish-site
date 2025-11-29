package com.dabwish.dabwish.service

import com.dabwish.dabwish.exception.InvalidCredentialsException
import com.dabwish.dabwish.exception.UserAlreadyExistsException
import com.dabwish.dabwish.exception.UsernameNotFoundException
import com.dabwish.dabwish.generated.dto.LoginRequest
import com.dabwish.dabwish.generated.dto.LoginResponse
import com.dabwish.dabwish.generated.dto.RegisterRequest
import com.dabwish.dabwish.mapper.AuthMapper
import com.dabwish.dabwish.repository.AuthRepository
import com.dabwish.dabwish.security.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val authRepository: AuthRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authMapper: AuthMapper,
    private val jwtTokenProvider: JwtTokenProvider,
) {

    fun login(request: LoginRequest): LoginResponse {
        val user = authRepository.findByName(request.name)
            ?: throw UsernameNotFoundException(request.name)

        if (!passwordEncoder.matches(request.password, user.hashPassword)) {
            throw InvalidCredentialsException("Invalid Credentials")
        }

        val token = jwtTokenProvider.generateToken(user)
        return authMapper.toLoginResponse(user, token, jwtTokenProvider.getExpirationSeconds())
    }

    fun register(request: RegisterRequest): LoginResponse {
        if (authRepository.existsByName(request.name)) {
            throw UserAlreadyExistsException(request.name)
        }

        val user = authMapper.registerRequestToUser(request)
        user.hashPassword = passwordEncoder.encode(request.password)
        val saved = authRepository.save(user)

        val token = jwtTokenProvider.generateToken(saved)
        return authMapper.toLoginResponse(saved, token, jwtTokenProvider.getExpirationSeconds())
    }
}