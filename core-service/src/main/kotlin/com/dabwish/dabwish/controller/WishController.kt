package com.dabwish.dabwish.controller

import com.dabwish.dabwish.generated.api.WishesApi
import com.dabwish.dabwish.generated.dto.WishPageResponse
import com.dabwish.dabwish.generated.dto.WishResponse
import com.dabwish.dabwish.generated.dto.WishUpdateRequest
import com.dabwish.dabwish.mapper.WishMapper
import com.dabwish.dabwish.service.WishService
import com.dabwish.dabwish.util.FileValidator
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile


@RestController
class WishController(
    private val wishService: WishService,
    private val wishMapper: WishMapper,
    ): WishesApi {

    override fun deleteWishById(wishId: Long): ResponseEntity<Unit> {
        wishService.delete(wishId)
        return ResponseEntity.ok().build()
    }

    override fun updateWishByIdWithFile(
        wishId: Long,
        title: String?,
        description: String?,
        photo: MultipartFile?,
        price: Double?
    ): ResponseEntity<WishResponse> {
        photo?.let { FileValidator.validateImage(it) }

        val updateRequest = WishUpdateRequest(
            title = title,
            description = description,
            price = price,
            photoUrl = null
        )

        val wish = wishService.updateWithFile(wishId, updateRequest, photo)

        return ResponseEntity.ok(wishMapper.toResponse(wish))
    }

    override fun getWishById(wishId: Long): ResponseEntity<WishResponse> {
        val wish = wishService.findById(wishId)
        val wishResponse = wishMapper.toResponse(wish)
        return ResponseEntity.ok(wishResponse)
    }

    override fun updateWishById(
        wishId: Long,
        wishUpdateRequest: WishUpdateRequest
    ): ResponseEntity<WishResponse> {
        val wish = wishService.update(wishId, wishUpdateRequest)
        val wishResponse = wishMapper.toResponse(wish)
        return ResponseEntity.ok(wishResponse)
    }

    override fun searchWishes(
        query: String,
        page: Int,
        size: Int
    ): ResponseEntity<WishPageResponse> {
        return super.searchWishes(query, page, size)
    }
}