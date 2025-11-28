package com.dabwish.dabwish.repository

import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.model.wish.Wish
import org.springframework.data.jpa.repository.JpaRepository

interface WishRepository: JpaRepository<Wish, Long> {
    fun findByUser(user: User): List<Wish>
}