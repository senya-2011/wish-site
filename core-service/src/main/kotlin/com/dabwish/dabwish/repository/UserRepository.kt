package com.dabwish.dabwish.repository

import com.dabwish.dabwish.model.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long>{
    fun findByName(name: String): User?
}