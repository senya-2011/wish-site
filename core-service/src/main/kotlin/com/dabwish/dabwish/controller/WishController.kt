package com.dabwish.dabwish.controller

import com.dabwish.dabwish.generated.api.WishesApi
import com.dabwish.dabwish.generated.dto.WishResponse
import com.dabwish.dabwish.generated.dto.WishUpdateRequest
import com.dabwish.dabwish.mapper.WishMapper
import com.dabwish.dabwish.service.WishService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController


@RestController
class WishController(
    private val wishService: WishService,
    private val wishMapper: WishMapper,
    ): WishesApi {

    override fun deleteWishById(wishId: Long): ResponseEntity<Unit> {
        wishService.delete(wishId)
        return ResponseEntity.ok().build()
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
}