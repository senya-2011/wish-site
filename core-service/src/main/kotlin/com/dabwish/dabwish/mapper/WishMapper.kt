package com.dabwish.dabwish.mapper

import com.dabwish.dabwish.generated.dto.WishRequest
import com.dabwish.dabwish.generated.dto.WishResponse
import com.dabwish.dabwish.generated.dto.WishUpdateRequest
import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.model.wish.Wish
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
}