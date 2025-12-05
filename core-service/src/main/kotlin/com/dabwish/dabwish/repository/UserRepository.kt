package com.dabwish.dabwish.repository

import com.dabwish.dabwish.model.user.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByName(name: String): User?
    fun existsByName(name: String): Boolean
    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<User>
}