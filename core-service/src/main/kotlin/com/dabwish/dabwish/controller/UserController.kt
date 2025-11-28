package com.dabwish.dabwish.controller

import com.dabwish.dabwish.generated.api.UsersApi
import com.dabwish.dabwish.generated.dto.UserRequest
import com.dabwish.dabwish.generated.dto.UserResponse
import com.dabwish.dabwish.generated.dto.UserUpdateRequest
import com.dabwish.dabwish.generated.dto.WishRequest
import com.dabwish.dabwish.generated.dto.WishResponse
import com.dabwish.dabwish.mapper.UserMapper
import com.dabwish.dabwish.mapper.WishMapper
import com.dabwish.dabwish.service.UserService
import com.dabwish.dabwish.service.WishService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userMapper: UserMapper,
    private val userService: UserService,
    private val wishService: WishService,
    private val wishMapper: WishMapper,
) : UsersApi{

    // GET
    override fun getAllUsers(): ResponseEntity<List<UserResponse>> {
        val users = userService.findAll()
        val response = userMapper.userListToUserResponseList(users)
        return ResponseEntity.ok(response)
    }

    override fun getUserById(userId: Long): ResponseEntity<UserResponse> {
        val user = userService.findById(userId)
        val userResponse = userMapper.userToUserResponse(user)
        return ResponseEntity.ok(userResponse)
    }

    // POST
    override fun createUser(userRequest: UserRequest): ResponseEntity<UserResponse> {
        val user = userService.create(userRequest)
        val userResponse = userMapper.userToUserResponse(user)
        return ResponseEntity.ok(userResponse)
    }

    // DELETE
    override fun deleteUserById(userId: Long): ResponseEntity<Unit> {
        userService.delete(userId)
        return ResponseEntity.ok().build()
    }

    // PATCH
    override fun updateUserById(
        userId: Long,
        userUpdateRequest: UserUpdateRequest
    ): ResponseEntity<UserResponse> {
        val user = userService.update(userId,userUpdateRequest)
        val userResponse = userMapper.userToUserResponse(user)
        return ResponseEntity.ok(userResponse)
    }

    // WISHES
    override fun createWish(
        userId: Long,
        wishRequest: WishRequest
    ): ResponseEntity<WishResponse> {
        val wish = wishService.create(userId, wishRequest)
        val wishResponse = wishMapper.toResponse(wish)
        return ResponseEntity.ok(wishResponse)
    }

    override fun getUserWishes(userId: Long): ResponseEntity<List<WishResponse>> {
        val wishes = wishService.findAllByUserId(userId)
        val wishesResponse = wishMapper.toResponseList(wishes)
        return ResponseEntity.ok(wishesResponse)
    }
}