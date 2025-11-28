package com.dabwish.dabwish.service

import com.dabwish.dabwish.events.WishEventPublisher
import com.dabwish.dabwish.exception.UserNotFoundException
import com.dabwish.dabwish.exception.WishNotFoundException
import com.dabwish.dabwish.generated.dto.WishRequest
import com.dabwish.dabwish.generated.dto.WishUpdateRequest
import com.dabwish.dabwish.mapper.WishMapper
import com.dabwish.dabwish.model.wish.Wish
import com.dabwish.dabwish.repository.UserRepository
import com.dabwish.dabwish.repository.WishRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service


@Service
class WishService(
    private val wishRepository: WishRepository,
    private val wishMapper: WishMapper,
    private val userRepository: UserRepository,
    @Autowired(required = false) private val wishEventPublisher: WishEventPublisher?,
) {
    fun findAllByUserId(userId: Long): List<Wish> {
        if (!userRepository.existsById(userId)) {
            throw UserNotFoundException(userId)
        }
        return wishRepository.findAllByUserId(userId)
    }

    fun findById(id: Long): Wish {
        return wishRepository.findById(id).
            orElseThrow{WishNotFoundException(id)}
    }

    @Transactional
    fun create(userId: Long, request: WishRequest): Wish {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException(userId) }
        val wish = wishMapper.toEntity(request, user)
        val saved  = wishRepository.save(wish)
        wishEventPublisher?.publishWishCreated(saved)
        return saved
    }

    @Transactional
    fun delete(id: Long){
        if (!wishRepository.existsById(id)) throw WishNotFoundException(id)
        wishRepository.deleteById(id)
    }

    @Transactional
    fun update(id: Long, wishUpdateRequest: WishUpdateRequest): Wish {
        val wish = findById(id)
        wishMapper.updateEntityFromRequest(wishUpdateRequest, wish)
        val saved = wishRepository.save(wish)
        wishEventPublisher?.publishWishUpdated(saved)
        return saved
    }
}