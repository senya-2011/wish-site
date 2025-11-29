package com.dabwish.dabwish.security

import com.dabwish.dabwish.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = resolveToken(request)

        if (token != null && jwtTokenProvider.validateToken(token)) {
            val userId = jwtTokenProvider.getUserId(token)
            val user = userId?.let { id -> userRepository.findById(id).orElse(null) }

            if (user != null && SecurityContextHolder.getContext().authentication == null) {
                val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
                val authentication = UsernamePasswordAuthenticationToken(user, null, authorities)
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val header = request.getHeader("Authorization") ?: return null
        if (!header.startsWith("Bearer ", ignoreCase = true)) return null
        return header.substringAfter("Bearer ").trim().takeIf { it.isNotEmpty() }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.servletPath.lowercase()
        return path.startsWith("/api/auth")
    }
}

