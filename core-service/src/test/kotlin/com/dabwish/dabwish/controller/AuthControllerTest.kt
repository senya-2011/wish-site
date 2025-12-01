package com.dabwish.dabwish.controller

import com.dabwish.dabwish.config.SecurityConfig
import com.dabwish.dabwish.generated.dto.LoginRequest
import com.dabwish.dabwish.generated.dto.LoginResponse
import com.dabwish.dabwish.generated.dto.RegisterRequest
import com.dabwish.dabwish.service.AuthService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(
    controllers = [AuthController::class],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [SecurityConfig::class],
        ),
    ],
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper,
) {

    @MockkBean
    private lateinit var authService: AuthService

    private val loginResponse = LoginResponse(accessToken = "jwt-token", expiresIn = 3600)

    @Test
    fun `login returns 200`() {
        val request = LoginRequest(name = "user", password = "pass")
        every { authService.login(request) } returns loginResponse

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.access_token") { value("jwt-token") }
        }
    }

    @Test
    fun `register returns 200`() {
        val request = RegisterRequest(name = "new", password = "pass123")
        every { authService.register(request) } returns loginResponse

        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.access_token") { value("jwt-token") }
        }
    }
}


