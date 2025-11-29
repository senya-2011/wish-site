package com.dabwish.dabwish.repository

import com.dabwish.dabwish.model.user.User
import org.springframework.stereotype.Repository

@Repository
class AuthRepository(
    private val userRepository: UserRepository,
) {
    fun findByName(name: String): User? = userRepository.findByName(name)

    fun save(user: User): User = userRepository.save(user)

    fun existsByName(name: String): Boolean = userRepository.existsByName(name)
}