package com.dabwish.dabwish.mapper

import com.dabwish.dabwish.generated.dto.WishRequest
import com.dabwish.dabwish.generated.dto.WishResponse
import com.dabwish.dabwish.generated.dto.WishUpdateRequest
import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.model.wish.Wish
import com.dabwish.dabwish.model.wish.WishDoc
import com.dabwish.events.wish.WishCreatedEvent
import com.dabwish.events.wish.WishUpdatedEvent
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy

@Mapper(componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface WishMapper {

    // GET
    @Mapping(source = "id", target= "wishId" )
    @Mapping(source = "user.id", target = "ownerId" )
    fun toResponse(wish: Wish): WishResponse

    fun toResponseList(wishes: List<Wish>): List<WishResponse>

    @Mapping(source = "wish.id", target = "wishId")
    @Mapping(source = "wish.user.id", target = "ownerId")
    @Mapping(source = "createdAtIso", target = "createdAt")
    fun toCreatedEvent(wish: Wish, createdAtIso: String): WishCreatedEvent

    @Mapping(source = "wish.id", target = "wishId")
    @Mapping(source = "wish.user.id", target = "ownerId")
    @Mapping(source = "updatedAtIso", target = "updatedAt")
    fun toUpdatedEvent(wish: Wish, updatedAtIso: String): WishUpdatedEvent

    // POST / Create

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", source = "user")
    fun toEntity(request: WishRequest, user: User): Wish


    // PATCH / Update
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    fun updateEntityFromRequest(request: WishUpdateRequest, @MappingTarget wish: Wish)

    // Elasticsearch
    @Mapping(source = "id", target = "id")
    @Mapping(source = "user.id", target = "ownerId")
    fun toDoc(wish: Wish): WishDoc
}