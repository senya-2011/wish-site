package com.dabwish.dabwish.controller

import com.dabwish.dabwish.generated.api.UsersApi
import com.dabwish.dabwish.generated.dto.UserRequest
import com.dabwish.dabwish.generated.dto.UserResponse
import com.dabwish.dabwish.generated.dto.UserUpdateRequest
import com.dabwish.dabwish.generated.dto.WishPageResponse
import com.dabwish.dabwish.generated.dto.WishRequest
import com.dabwish.dabwish.generated.dto.WishResponse
import com.dabwish.dabwish.mapper.UserMapper
import com.dabwish.dabwish.mapper.WishMapper
import com.dabwish.dabwish.service.UserService
import com.dabwish.dabwish.service.WishService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
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
    @PreAuthorize("hasRole('ADMIN')")
    override fun createUser(userRequest: UserRequest): ResponseEntity<UserResponse> {
        val user = userService.create(userRequest)
        val userResponse = userMapper.userToUserResponse(user)
        return ResponseEntity.ok(userResponse)
    }

    // DELETE
    @PreAuthorize("hasRole('ADMIN')")
    override fun deleteUserById(userId: Long): ResponseEntity<Unit> {
        userService.delete(userId)
        return ResponseEntity.ok().build()
    }

    // PATCH
    @PreAuthorize("hasRole('ADMIN')")
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

    override fun getUserWishes(userId: Long, page: Int, size: Int): ResponseEntity<WishPageResponse> {
        val pageNumber = page.coerceAtLeast(0)
        val pageSize = size.coerceIn(1, 50)
        val pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val wishesPage = wishService.findAllByUserId(userId, pageable)
        val dto = WishPageResponse(
            items = wishMapper.toResponseList(wishesPage.content),
            page = wishesPage.number,
            propertySize = wishesPage.size,
            totalElements = wishesPage.totalElements,
            totalPages = wishesPage.totalPages,
        )
        return ResponseEntity.ok(dto)
    }
}