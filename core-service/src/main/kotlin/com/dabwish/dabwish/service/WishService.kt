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
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WishService(
    private val wishRepository: WishRepository,
    private val wishMapper: WishMapper,
    private val userRepository: UserRepository,
    @Autowired(required = false) private val wishEventPublisher: WishEventPublisher?,
) {
    @Cacheable(
        cacheNames = ["userWishes"],
        key = "#userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()",
    )
    fun findAllByUserId(userId: Long, pageable: Pageable): Page<Wish> {
        if (!userRepository.existsById(userId)) {
            throw UserNotFoundException(userId)
        }
        return wishRepository.findAllByUserId(userId, pageable)
    }

    @Cacheable(cacheNames = ["wishesById"], key = "#id")
    fun findById(id: Long): Wish {
        return wishRepository.findById(id)
            .orElseThrow { WishNotFoundException(id) }
    }

    @Transactional
    @Caching(
        put = [CachePut(cacheNames = ["wishesById"], key = "#result.id")],
        evict = [CacheEvict(cacheNames = ["userWishes"], allEntries = true)],
    )
    fun create(userId: Long, request: WishRequest): Wish {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException(userId) }
        val wish = wishMapper.toEntity(request, user)
        val saved = wishRepository.save(wish)
        wishEventPublisher?.publishWishCreated(saved)
        return saved
    }

    @Transactional
    @Caching(
        evict = [
            CacheEvict(cacheNames = ["wishesById"], key = "#id"),
            CacheEvict(cacheNames = ["userWishes"], allEntries = true),
        ],
    )
    fun delete(id: Long) {
        if (!wishRepository.existsById(id)) throw WishNotFoundException(id)
        wishRepository.deleteById(id)
    }

    @Transactional
    @Caching(
        put = [CachePut(cacheNames = ["wishesById"], key = "#result.id")],
        evict = [CacheEvict(cacheNames = ["userWishes"], allEntries = true)],
    )
    fun update(id: Long, wishUpdateRequest: WishUpdateRequest): Wish {
        val wish = wishRepository.findById(id).orElseThrow{WishNotFoundException(id)}
        wishMapper.updateEntityFromRequest(wishUpdateRequest, wish)
        val saved = wishRepository.save(wish)
        wishEventPublisher?.publishWishUpdated(saved)
        return saved
    }
}