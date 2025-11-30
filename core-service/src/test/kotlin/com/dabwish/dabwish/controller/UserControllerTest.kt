package com.dabwish.dabwish.controller

import com.dabwish.dabwish.config.SecurityConfig
import com.dabwish.dabwish.generated.dto.UserRequest
import com.dabwish.dabwish.generated.dto.UserResponse
import com.dabwish.dabwish.generated.dto.UserUpdateRequest
import com.dabwish.dabwish.generated.dto.WishRequest
import com.dabwish.dabwish.generated.dto.WishResponse
import com.dabwish.dabwish.mapper.UserMapper
import com.dabwish.dabwish.mapper.WishMapper
import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.model.user.UserRole
import com.dabwish.dabwish.model.wish.Wish
import com.dabwish.dabwish.service.UserService
import com.dabwish.dabwish.service.WishService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.http.MediaType
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import java.time.OffsetDateTime


@WebMvcTest(
    controllers = [UserController::class],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [SecurityConfig::class],
        ),
    ],
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val objectMapper: ObjectMapper,
) {
    @MockkBean
    private lateinit var userService: UserService

    @MockkBean
    private lateinit var userMapper: UserMapper

    @MockkBean
    private lateinit var wishService: WishService

    @MockkBean
    private lateinit var wishMapper: WishMapper

    val userRequest = UserRequest(name = "user", password = "pass")
    val user = User(id = 1, name = "user", role = UserRole.MEMBER)
    val userResponse = UserResponse(
        userId = user.id,
        name = user.name,
        createdAt = OffsetDateTime.now(),
        role = UserResponse.Role.member,
    )

    val users = listOf(User(id = 1, name = "user", role = UserRole.MEMBER), User(id = 2, name = "Ivan", role = UserRole.ADMIN))

    val responseUsers = listOf(
        UserResponse(userId = 1, name = "user", role = UserResponse.Role.member, createdAt = OffsetDateTime.now(),),
        UserResponse(userId = 2, name = "Ivan", role = UserResponse.Role.admin, createdAt = OffsetDateTime.now(),),
    )

    //GetAllUsers
    @Test
    fun `getAllUsers return 200 and list of all users`(){
        every { userService.findAll() } returns users
        every { userMapper.userListToUserResponseList(users) } returns responseUsers

        mockMvc.get("/api/users") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.[0].user_id") { value(1) }
            jsonPath("$[0].name") { value("user") }
            jsonPath("$.[1].user_id") { value(2) }
            jsonPath("$[1].name") { value("Ivan") }
        }

        verify(exactly = 1) { userService.findAll() }
    }

    //Get User By id
    @Test
    fun `get user by id return 200 when user exists`(){

        every { userService.findById(1) } returns user
        every { userMapper.userToUserResponse(user) } returns userResponse

        mockMvc.get("/api/users/${user.id}"){
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.name") { value(user.name) }
            jsonPath("$.user_id") { value(user.id) }
        }

        verify(exactly = 1) { userService.findById(user.id) }
    }

    //Post create user
    @Test
    fun `create new user return 200 and user`(){
        every { userService.create(userRequest) } returns user
        every { userMapper.userToUserResponse(user) } returns userResponse

        mockMvc.post("/api/users") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(userRequest)
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.name") { value(userRequest.name) }
        }

        verify(exactly = 1) { userService.create(userRequest) }
    }

    //Delete user by id
    @Test
    fun `delete user by id and return 200`(){
        every {userService.delete(user.id)} returns Unit
        mockMvc.delete("/api/users/${user.id}"){
            contentType = MediaType.APPLICATION_JSON
        }.andExpect { status { isOk() } }

        verify(exactly = 1) { userService.delete(user.id) }
    }

    // Update Name Test
    @Test
    fun `update user Name return 200 + updatedUser`(){
        val userUpdateRequest = UserUpdateRequest(name = "New name")
        val userForUpdate = User(id = 1, name = "New name", role = UserRole.MEMBER)
        val userUpdateResponse = UserResponse(
            userId = userForUpdate.id,
            name = userForUpdate.name,
            createdAt = OffsetDateTime.now(),
            role = UserResponse.Role.member,
            )

        every { userService.update(userForUpdate.id, userUpdateRequest) } returns userForUpdate
        every { userMapper.userToUserResponse(userForUpdate)} returns userUpdateResponse

        mockMvc.patch("/api/users/${userForUpdate.id}") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(  userUpdateRequest)
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.name") { value(userUpdateRequest.name) }
        }

        verify(exactly = 1) { userService.update(userForUpdate.id, userUpdateRequest) }
    }

    // --- WISHES ---
    // GET user wishes
    @Test
    fun `getUserWishes by user id returns paged payload`() {
        val wishes = listOf(
            Wish(id = 1, title = "PS5", user = user),
            Wish(id = 2, title = "PS5Pro", user = user),
        )
        val wishesResponse = listOf(
            WishResponse(wishId = 1, ownerId = 1, title = "PS5", createdAt = OffsetDateTime.now()),
            WishResponse(wishId = 2, ownerId = 1, title = "PS5Pro", createdAt = OffsetDateTime.now()),
        )
        val pageable = PageRequest.of(0, 10)
        every { wishService.findAllByUserId(eq(user.id), any()) } returns PageImpl(wishes, pageable, wishes.size.toLong())
        every { wishMapper.toResponseList(wishes) } returns wishesResponse

        mockMvc.get("/api/users/${user.id}/wishes?page=0&size=10") {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.items[0].title") { value(wishesResponse.first().title) }
            jsonPath("$.items[1].title") { value(wishesResponse[1].title) }
            jsonPath("$.page") { value(0) }
            jsonPath("$.total_pages") { value(1) }
        }

        verify(exactly = 1) { wishService.findAllByUserId(eq(user.id), any()) }
    }

    // POST create new wish
    @Test
    fun `create new wish return 200 and Wish`(){
        val wishRequest = WishRequest(
            title = "PS5"
        )
        val wish = Wish(id=1, title=wishRequest.title, user=user)
        val wishResponse = WishResponse(
            wishId = wish.id,
            ownerId = user.id,
            title = wish.title,
            createdAt = OffsetDateTime.now(),
        )

        every { wishService.create(user.id, wishRequest)} returns wish
        every { wishMapper.toResponse(wish) } returns wishResponse

        mockMvc.post("/api/users/${user.id}/wishes") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(wishRequest)
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.title") { value(wishResponse.title) }
        }

        verify(exactly = 1) { wishService.create(user.id, wishRequest) }
    }
}