package com.dabwish.dabwish.service

import com.dabwish.dabwish.events.UserEventPublisher
import com.dabwish.dabwish.exception.UserNotFoundException
import com.dabwish.dabwish.generated.dto.UserRequest
import com.dabwish.dabwish.generated.dto.UserUpdateRequest
import com.dabwish.dabwish.mapper.UserMapper
import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.model.user.UserRole
import com.dabwish.dabwish.repository.UserRepository
import io.mockk.Runs
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class UserServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val userMapper = mockk<UserMapper>(relaxUnitFun = true)

    private val userEventPublisher = mockk<UserEventPublisher>(relaxed = true)

    private val userService = UserService(userRepository, userMapper, userEventPublisher)

    @BeforeEach
    fun setUp() {
        clearMocks(userRepository, userMapper, userEventPublisher)
    }

    // --- FIND ALL ---
    @Test
    fun `findAll should return list of users`() {
        val users = listOf(User(id = 1, name = "Test", role = UserRole.MEMBER))
        every { userRepository.findAll() } returns users

        val result = userService.findAll()

        assertEquals(1, result.size)
        assertEquals("Test", result[0].name)
        verify(exactly = 1) { userRepository.findAll() }
    }

    // --- FIND BY ID ---
    @Test
    fun `findById should return user when exists`() {
        val userId = 1L
        val user = User(id = userId, name = "Test", role = UserRole.MEMBER)

        every { userRepository.findById(userId) } returns Optional.of(user)

        val result = userService.findById(userId)

        assertNotNull(result)
        assertEquals(userId, result.id)
    }

    @Test
    fun `findById should throw UserNotFoundException when not exists`() {
        val userId = 999L
        every { userRepository.findById(userId) } returns Optional.empty()

        assertThrows<UserNotFoundException> {
            userService.findById(userId)
        }
    }

    // --- CREATE ---
    @Test
    fun `create should save user and publish event`() {
        val request = UserRequest(name = "New", password = "pwd")
        val newUser = User(name = "New", role = UserRole.MEMBER)
        val savedUser = newUser.copy(id = 10)

        every { userMapper.userRequestToUser(request) } returns newUser
        every { userRepository.save(newUser) } returns savedUser

        val result = userService.create(request)


        assertEquals(10, result.id)

        verifySequence {
            userMapper.userRequestToUser(request)
            userRepository.save(newUser)
            userEventPublisher.publishUserCreated(savedUser)
        }
    }

    // --- DELETE ---
    @Test
    fun `delete should call repo deleteById when user exists`() {
        val userId = 1L
        every { userRepository.existsById(userId) } returns true
        every { userRepository.deleteById(userId) } just Runs

        userService.delete(userId)

        verify { userRepository.deleteById(userId) }
    }

    @Test
    fun `delete should throw exception when user does not exist`() {
        val userId = 1L
        every { userRepository.existsById(userId) } returns false

        assertThrows<UserNotFoundException> {
            userService.delete(userId)
        }

        verify(exactly = 0) { userRepository.deleteById(any()) }
    }

    // --- UPDATE ---
    @Test
    fun `update should modify and save user`() {
        val userId = 1L
        val updateRequest = UserUpdateRequest(name = "Updated Name")
        val existingUser = User(id = userId, name = "Old Name", role = UserRole.MEMBER)

        every { userRepository.findById(userId) } returns Optional.of(existingUser)

        every { userRepository.save(any()) } answers { firstArg() }

        val result = userService.update(userId, updateRequest)

        verify { userMapper.updateUserFromRequest(updateRequest, existingUser) }

        verify { userRepository.save(existingUser) }
    }
}