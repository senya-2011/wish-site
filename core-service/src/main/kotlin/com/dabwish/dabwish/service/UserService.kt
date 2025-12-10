package com.dabwish.dabwish.service

import com.dabwish.dabwish.events.UserEventPublisher
import com.dabwish.dabwish.exception.UserNotFoundException
import com.dabwish.dabwish.generated.dto.UserRequest
import com.dabwish.dabwish.generated.dto.UserUpdateRequest
import com.dabwish.dabwish.mapper.UserMapper
import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
    @Autowired(required = false) private val userEventPublisher: UserEventPublisher?,
) {
    fun findAll(): List<User> = userRepository.findAll()

    @Cacheable(cacheNames = ["usersById"], key = "#id")
    fun findById(id: Long): User {
        return userRepository.findById(id).
                orElseThrow{ UserNotFoundException(id) }
    }

    @Transactional
    @Caching(
        evict = [CacheEvict(cacheNames = ["userSearch"], allEntries = true)],
    )
    fun create(userRequest: UserRequest): User {
        val user = userMapper.userRequestToUser(userRequest)
        val saved = userRepository.save(user)
        userEventPublisher?.publishUserCreated(saved)
        return saved
    }

    @Transactional
    @Caching(
        evict = [
            CacheEvict(cacheNames = ["usersById"], key = "#id"),
            CacheEvict(cacheNames = ["userWishes"], allEntries = true),
            CacheEvict(cacheNames = ["userSearch"], allEntries = true),
        ],
    )
    fun delete(id: Long){
        if (!userRepository.existsById(id)) throw UserNotFoundException(id)
        userRepository.deleteById(id)
    }

    @Transactional
    @CacheEvict(cacheNames = ["usersById"], key = "#id")
    fun update(id: Long, userUpdateRequest: UserUpdateRequest): User {
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException(id) }
        userMapper.updateUserFromRequest(userUpdateRequest, user)
        return userRepository.save(user)
    }

    fun searchByName(query: String, pageable: Pageable, excludeUserId: Long? = null): Page<User> {
        val usersPage = userRepository.findByNameContainingIgnoreCase(query, pageable)
        if (excludeUserId == null) {
            return usersPage
        }
        val filteredContent = usersPage.content.filter { it.id != excludeUserId }
        val adjustedTotal = if (filteredContent.size < usersPage.content.size) {
            maxOf(0, usersPage.totalElements - 1)
        } else {
            usersPage.totalElements
        }
        return org.springframework.data.domain.PageImpl(
            filteredContent,
            pageable,
            adjustedTotal
        )
    }
}