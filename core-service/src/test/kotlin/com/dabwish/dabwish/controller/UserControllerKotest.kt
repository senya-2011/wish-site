package com.dabwish.dabwish.controller

import com.dabwish.dabwish.generated.dto.WishResponse
import com.dabwish.dabwish.mapper.UserMapper
import com.dabwish.dabwish.mapper.WishMapper
import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.model.user.UserRole
import com.dabwish.dabwish.model.wish.Wish
import com.dabwish.dabwish.service.UserService
import com.dabwish.dabwish.service.WishService
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.byte
import io.kotest.property.arbitrary.byteArray
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.every
import io.mockk.clearMocks
import io.mockk.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.multipart
import java.time.OffsetDateTime


@WebMvcTest(controllers = [UserController::class])
@AutoConfigureMockMvc(addFilters = false)
class UserControllerKotest : BehaviorSpec() {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockkBean
    lateinit var userService: UserService

    @MockkBean
    lateinit var userMapper: UserMapper

    @MockkBean
    lateinit var wishService: WishService

    @MockkBean
    lateinit var wishMapper: WishMapper

    override fun extensions() = listOf(SpringExtension)

    private data class WishWithFileInput(
        val userId: Long,
        val title: String,
        val description: String?,
        val price: Double,
        val photo: MockMultipartFile
    )

    private val wishWithFileGenerator = Arb.bind(
        Arb.long(min = 1L, max = 50L),
        Arb.string(minSize = 3, maxSize = 20),
        Arb.double(min = 1.0, max = 5000.0),
        Arb.string(minSize = 0, maxSize = 50),
        Arb.byteArray(Arb.int(10, 200), Arb.byte())
    ) { userId, title, price, description, bytes ->
        WishWithFileInput(
            userId = userId,
            title = title,
            description = description.ifBlank { null },
            price = price,
            photo = MockMultipartFile(
                "photo",
                "$title.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                bytes
            )
        )
    }

    init {

        Given("Wish creation with file") {

            When("Randomized requests are sent") {

                Then("Request is processed correctly") {

                    checkAll(10,wishWithFileGenerator) { generated ->

                        clearMocks(wishService, wishMapper, answers = false)

                        val wish = Wish(
                            id = 1,
                            title = generated.title,
                            user = User(id = generated.userId, name = "test", role = UserRole.MEMBER)
                        )

                        val wishResponse = WishResponse(
                            wishId = 1,
                            ownerId = generated.userId,
                            title = generated.title,
                            createdAt = OffsetDateTime.now()
                        )

                        every { wishService.createWithFile(eq(generated.userId), any(), any()) } returns wish
                        every { wishMapper.toResponse(wish) } returns wishResponse

                        mockMvc.multipart("/api/users/${generated.userId}/wishes/with-file") {
                            file(generated.photo)
                            param("title", generated.title)
                            generated.description?.let { param("description", it) }
                            param("price", generated.price.toString())
                        }.andExpect {
                            status { isOk() }
                            content { contentType(MediaType.APPLICATION_JSON) }
                            jsonPath("$.title") { value(generated.title) }
                            jsonPath("$.owner_id") { value(generated.userId) }
                        }

                        verify(exactly = 1) {
                            wishService.createWithFile(
                                eq(generated.userId),
                                match {
                                    it.title == generated.title &&
                                            it.price == generated.price &&
                                            it.description == generated.description
                                },
                                any()
                            )
                        }
                    }
                }
            }
        }
    }
}
