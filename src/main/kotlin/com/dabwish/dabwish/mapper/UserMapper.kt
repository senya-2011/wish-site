package com.dabwish.dabwish.mapper

import com.dabwish.dabwish.generated.dto.UserRequest
import com.dabwish.dabwish.generated.dto.UserResponse
import com.dabwish.dabwish.generated.dto.UserUpdateRequest
import com.dabwish.dabwish.model.user.User
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.NullValuePropertyMappingStrategy

@Mapper(componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface UserMapper {

    //GET
    @Mapping(source = "id", target = "userId")
    fun userToUserResponse(user: User): UserResponse
    fun userListToUserResponseList(users: List<User>): List<UserResponse>


    // POST / CREATE
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "hashPassword", ignore = true)
    @Mapping(target = "role", constant = "MEMBER")
    fun userRequestToUser(userRequest: UserRequest): User


    // PATCH / Update
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "hashPassword", ignore = true)
    fun updateUserFromRequest(request: UserUpdateRequest, @MappingTarget user: User)
}