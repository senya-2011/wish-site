package com.dabwish.dabwish.repository

import com.dabwish.dabwish.model.user.TelegramVerificationCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime

@Repository
interface TelegramVerificationCodeRepository : JpaRepository<TelegramVerificationCode, Long> {
    fun findByUserId(userId: Long): TelegramVerificationCode?
    fun findByVerificationCode(verificationCode: String): TelegramVerificationCode?
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    fun deleteByUserId(userId: Long)
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TelegramVerificationCode t WHERE t.expiresAt < :expiresAt")
    fun deleteByExpiresAtBefore(@Param("expiresAt") expiresAt: OffsetDateTime)
}

