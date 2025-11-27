package com.dabwish.dabwish.repository

import com.dabwish.dabwish.data.user.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long>{
    fun findByName(name: String): User?
}