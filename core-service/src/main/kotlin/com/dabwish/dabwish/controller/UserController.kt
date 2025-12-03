package com.dabwish.dabwish.controller

import com.dabwish.dabwish.generated.api.UsersApi
import com.dabwish.dabwish.generated.dto.*
import com.dabwish.dabwish.mapper.UserMapper
import com.dabwish.dabwish.mapper.WishMapper
import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.service.TelegramVerificationService
import com.dabwish.dabwish.service.UserService
import com.dabwish.dabwish.service.UserSubscriptionService
import com.dabwish.dabwish.service.WishService
import com.dabwish.dabwish.util.FileValidator
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class UserController(
    private val userMapper: UserMapper,
    private val userService: UserService,
    private val wishService: WishService,
    private val wishMapper: WishMapper,
    private val telegramVerificationService: TelegramVerificationService,
    private val userSubscriptionService: UserSubscriptionService,
) : UsersApi{

    private fun getCurrentUserId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
        val user = authentication?.principal as? User
            ?: throw IllegalStateException("User not found in security context")
        return user.id
    }

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

    override fun createWishWithFile(
        userId: Long,
        title: String,
        description: String?,
        photo: MultipartFile?,
        price: Double?
    ): ResponseEntity<WishResponse> {
        photo?.let { FileValidator.validateImage(it) }

        val wishRequest = WishRequest(
            title = title,
            description = description,
            photoUrl = null,
            price = price
        )
        
        val wish = wishService.createWithFile(userId, wishRequest, photo)
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

    override fun searchUsers(
        query: String,
        page: Int,
        size: Int
    ): ResponseEntity<UserPageResponse> {
        val pageNumber = page.coerceAtLeast(0)
        val pageSize = size.coerceIn(1, 50)
        val pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.ASC, "name"))
        val usersPage = userService.searchByName(query, pageable)
        val dto = UserPageResponse(
            items = userMapper.userListToUserResponseList(usersPage.content),
            page = usersPage.number,
            propertySize = usersPage.size,
            totalElements = usersPage.totalElements,
            totalPages = usersPage.totalPages,
        )
        return ResponseEntity.ok(dto)
    }

    // TELEGRAM VERIFICATION
    override fun verifyTelegram(telegramVerificationRequest: TelegramVerificationRequest): ResponseEntity<TelegramVerificationResponse> {
        val userId = getCurrentUserId()
        telegramVerificationService.requestVerification(userId, telegramVerificationRequest.telegramUsername)
        val response = TelegramVerificationResponse(
            success = true,
            message = "Код верификации отправлен в Telegram бот"
        )
        return ResponseEntity.ok(response)
    }

    override fun confirmTelegram(telegramVerificationConfirmRequest: TelegramVerificationConfirmRequest): ResponseEntity<TelegramVerificationResponse> {
        val userId = getCurrentUserId()
        telegramVerificationService.confirmVerification(userId, telegramVerificationConfirmRequest.verificationCode)
        val response = TelegramVerificationResponse(
            success = true,
            message = "Telegram успешно привязан"
        )
        return ResponseEntity.ok(response)
    }

    // SUBSCRIPTIONS
    override fun subscribeToUser(userId: Long): ResponseEntity<SubscriptionResponse> {
        val subscriberId = getCurrentUserId()
        val subscription = userSubscriptionService.subscribe(subscriberId, userId)
        val response = SubscriptionResponse(
            subscriberId = subscription.subscriber.id,
            subscribedToId = subscription.subscribedTo.id,
            createdAt = subscription.createdAt
        )
        return ResponseEntity.ok(response)
    }

    override fun unsubscribeFromUser(userId: Long): ResponseEntity<Unit> {
        val subscriberId = getCurrentUserId()
        userSubscriptionService.unsubscribe(subscriberId, userId)
        return ResponseEntity.ok().build()
    }

    override fun getMySubscriptions(page: Int, size: Int): ResponseEntity<UserPageResponse> {
        val userId = getCurrentUserId()
        val pageNumber = page.coerceAtLeast(0)
        val pageSize = size.coerceIn(1, 50)
        val pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val usersPage = userSubscriptionService.getSubscriptions(userId, pageable)
        val dto = UserPageResponse(
            items = userMapper.userListToUserResponseList(usersPage.content),
            page = usersPage.number,
            propertySize = usersPage.size,
            totalElements = usersPage.totalElements,
            totalPages = usersPage.totalPages,
        )
        return ResponseEntity.ok(dto)
    }
}