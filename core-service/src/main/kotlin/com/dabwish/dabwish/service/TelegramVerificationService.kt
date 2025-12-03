package com.dabwish.dabwish.service

import com.dabwish.dabwish.events.TelegramEventPublisher
import com.dabwish.dabwish.exception.TelegramAlreadyVerifiedException
import com.dabwish.dabwish.exception.TelegramVerificationCodeExpiredException
import com.dabwish.dabwish.exception.TelegramVerificationCodeInvalidException
import com.dabwish.dabwish.exception.UserNotFoundException
import com.dabwish.dabwish.model.user.TelegramVerificationCode
import com.dabwish.dabwish.repository.TelegramVerificationCodeRepository
import com.dabwish.dabwish.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.Random

@Service
class TelegramVerificationService(
    private val userRepository: UserRepository,
    private val telegramVerificationCodeRepository: TelegramVerificationCodeRepository,
    @Autowired(required = false) private val telegramEventPublisher: TelegramEventPublisher?,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val random = Random()
    private val codeExpirationMinutes = 10L

    @Transactional
    fun requestVerification(userId: Long, telegramUsername: String): String {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException(userId) }

        if (user.telegramUsername != null) {
            throw TelegramAlreadyVerifiedException(userId)
        }

        val normalizedUsername = telegramUsername.removePrefix("@").trim()

        val verificationCode = generateVerificationCode()

        telegramVerificationCodeRepository.deleteByUserId(userId)

        val expiresAt = OffsetDateTime.now().plusMinutes(codeExpirationMinutes)
        val codeEntity = TelegramVerificationCode(
            user = user,
            telegramUsername = normalizedUsername,
            verificationCode = verificationCode,
            expiresAt = expiresAt,
        )
        telegramVerificationCodeRepository.save(codeEntity)

        telegramEventPublisher?.publishVerificationCode(userId, normalizedUsername, verificationCode)

        logger.info("Verification code generated for user $userId, telegram: $normalizedUsername")
        return verificationCode
    }

    @Transactional
    fun confirmVerification(userId: Long, code: String): Boolean {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException(userId) }

        val verificationCode = telegramVerificationCodeRepository.findByVerificationCode(code)
            ?: throw TelegramVerificationCodeInvalidException()

        if (verificationCode.user.id != userId) {
            throw TelegramVerificationCodeInvalidException()
        }

        if (verificationCode.expiresAt.isBefore(OffsetDateTime.now())) {
            telegramVerificationCodeRepository.deleteByUserId(userId)
            throw TelegramVerificationCodeExpiredException()
        }

        user.telegramUsername = verificationCode.telegramUsername
        userRepository.save(user)

        telegramVerificationCodeRepository.deleteByUserId(userId)

        logger.info("Telegram verified for user $userId, telegram: ${verificationCode.telegramUsername}")
        return true
    }

    private fun generateVerificationCode(): String {
        return (100000 + random.nextInt(900000)).toString()
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    fun cleanupExpiredCodes() {
        val deleted = telegramVerificationCodeRepository.deleteByExpiresAtBefore(OffsetDateTime.now())
        if (deleted > 0) {
            logger.debug("Cleaned up $deleted expired verification codes")
        }
    }
}

