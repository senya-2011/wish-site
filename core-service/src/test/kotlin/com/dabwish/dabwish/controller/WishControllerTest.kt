package com.dabwish.dabwish.controller

import com.dabwish.dabwish.config.SecurityConfig
import com.dabwish.dabwish.generated.dto.WishRequest
import com.dabwish.dabwish.generated.dto.WishResponse
import com.dabwish.dabwish.generated.dto.WishUpdateRequest
import com.dabwish.dabwish.mapper.WishMapper
import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.model.user.UserRole
import com.dabwish.dabwish.model.wish.Wish
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
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.patch
import java.time.OffsetDateTime


@WebMvcTest(
    controllers = [WishController::class],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = [SecurityConfig::class],
        ),
    ],
)
@AutoConfigureMockMvc(addFilters = false)
class WishControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper
) {
    @MockkBean
    private lateinit var wishService: WishService

    @MockkBean
    private lateinit var wishMapper: WishMapper

    val user = User(
        id = 1,
        name = "user",
        role = UserRole.MEMBER,
    )

    val wishRequest = WishRequest(
        title = "PS5",
    )

    val wish = Wish(
        id = 1,
        title = wishRequest.title,
        user = user
    )

    val wishResponse = WishResponse(
        wishId = wish.id,
        ownerId = user.id,
        title = wish.title,
        createdAt = OffsetDateTime.now()
    )

    //Get wish by id
    @Test
    fun `get Wish by id return 200 + wish`(){
        every { wishService.findById(wish.id) } returns wish
        every { wishMapper.toResponse(wish) } returns wishResponse

        mockMvc.get("/api/wishes/${wish.id}"){
            accept(MediaType.APPLICATION_JSON)
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.title") { value(wish.title) }
            jsonPath("$.owner_id") { value(wish.user.id) }
        }

        verify(exactly = 1) { wishService.findById(wish.id) }
    }


    // DELETE wish by id
    @Test
    fun `delete wish by id return 200`(){
        every {wishService.delete(wish.id) } returns Unit

        mockMvc.delete("/api/wishes/${wish.id}"){
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
        }

        verify(exactly = 1) { wishService.delete(wish.id) }
    }


    // Update wish by id
    @Test
    fun `update wish by id return 200`(){
        val wishUpdateRequest = WishUpdateRequest(
            title = "PS5Pro",
        )
        val wishUpdated = Wish(
            id = wish.id,
            title = "PS5Pro",
            user = wish.user,
        )
        val wishUpdatedResponse = WishResponse(
            wishId = wishUpdated.id,
            title = wishUpdated.title,
            createdAt = OffsetDateTime.now(),
            ownerId = wishUpdated.user.id,
        )

        every { wishService.update(wish.id, wishUpdateRequest) } returns wishUpdated
        every { wishMapper.toResponse(wishUpdated) } returns wishUpdatedResponse

        mockMvc.patch("/api/wishes/${wish.id}") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(wishUpdateRequest)
        }.andExpect {
            status { isOk() }
            content {contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.title") { value(wishUpdated.title) }
        }

        verify { wishService.update(wish.id, wishUpdateRequest) }
    }

    @Test
    fun `update wish with file returns 200`() {
        val file = MockMultipartFile(
            "photo",
            "new_image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "new content".toByteArray()
        )

        val updatedWish = wish.copy(title = "Updated Title")
        val updatedResponse = wishResponse.copy(title = "Updated Title")

        every { wishService.updateWithFile(eq(wish.id), any(), any()) } returns updatedWish
        every { wishMapper.toResponse(updatedWish) } returns updatedResponse

        mockMvc.multipart("/api/wishes/${wish.id}/with-file") {
            file(file)
            param("title", "Updated Title")
            with { request ->
                request.method = "PATCH"
                request
            }
        }.andExpect {
            status { isOk() }
            jsonPath("$.title") { value(updatedResponse.title) }
        }

        verify(exactly = 1) {
            wishService.updateWithFile(
                eq(wish.id),
                match { it.title == "Updated Title" },
                any()
            )
        }
    }
}