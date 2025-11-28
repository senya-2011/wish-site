package com.dabwish.dabwish.service

import com.dabwish.dabwish.events.UserEventPublisher
import com.dabwish.dabwish.exception.UserNotFoundException
import com.dabwish.dabwish.generated.dto.UserRequest
import com.dabwish.dabwish.generated.dto.UserUpdateRequest
import com.dabwish.dabwish.mapper.UserMapper
import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.repository.UserRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userMapper: UserMapper,
    @Autowired(required = false) private val userEventPublisher: UserEventPublisher?,
) {
    fun findAll(): List<User> = userRepository.findAll()

    fun findById(id: Long): User {
        return userRepository.findById(id).
                orElseThrow{ UserNotFoundException(id) }
    }

    @Transactional
    fun create(userRequest: UserRequest): User {
        val user = userMapper.userRequestToUser(userRequest)
        val saved = userRepository.save(user)
        userEventPublisher?.publishUserCreated(saved)
        return saved
    }

    @Transactional
    fun delete(id: Long){
        if (!userRepository.existsById(id)) throw UserNotFoundException(id)
        userRepository.deleteById(id)
    }

    @Transactional
    fun update(id: Long, userUpdateRequest: UserUpdateRequest): User {
        val user = findById(id)
        userMapper.updateUserFromRequest(userUpdateRequest, user)
        return userRepository.save(user)
    }
}