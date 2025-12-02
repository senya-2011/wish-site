package com.dabwish.dabwish.repository

import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.model.wish.Wish
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WishRepository : JpaRepository<Wish, Long> {
    fun findByUser(user: User): List<Wish>
    fun findAllByUserId(userId: Long): List<Wish>
    fun findAllByUserId(userId: Long, pageable: Pageable): Page<Wish>
    fun countByPhotoUrl(photoUrl: String): Long
}