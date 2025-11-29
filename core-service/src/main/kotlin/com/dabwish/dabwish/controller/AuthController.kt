package com.dabwish.dabwish.controller

import com.dabwish.dabwish.generated.api.AuthApi
import com.dabwish.dabwish.generated.dto.LoginRequest
import com.dabwish.dabwish.generated.dto.LoginResponse
import com.dabwish.dabwish.generated.dto.RegisterRequest
import com.dabwish.dabwish.mapper.AuthMapper
import com.dabwish.dabwish.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController


@RestController
class AuthController(
    private val authService: AuthService,
    private val authMapper: AuthMapper
): AuthApi {
    override fun login(loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        return super.login(loginRequest)
    }

    override fun register(registerRequest: RegisterRequest): ResponseEntity<LoginResponse> {
        return super.register(registerRequest)
    }
}