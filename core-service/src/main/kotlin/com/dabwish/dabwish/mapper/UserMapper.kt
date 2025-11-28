package com.dabwish.dabwish.mapper

import com.dabwish.dabwish.generated.dto.UserRequest
import com.dabwish.dabwish.generated.dto.UserResponse
import com.dabwish.dabwish.generated.dto.UserUpdateRequest
import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.model.user.UserRole
import com.dabwish.events.user.UserCreatedEvent
import com.dabwish.events.user.UserRole as AvroUserRole
import org.mapstruct.AfterMapping
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget
import org.mapstruct.Named
import org.mapstruct.NullValuePropertyMappingStrategy
import org.mapstruct.ValueMapping
import org.mapstruct.ValueMappings
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Mapper(componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
abstract class UserMapper {

    @Autowired
    protected lateinit var passwordEncoder: BCryptPasswordEncoder

    //GET
    @Mapping(source = "id", target = "userId")
    @Mapping(source = "role", target = "role", qualifiedByName = ["userRoleToResponseRole"])
    abstract fun userToUserResponse(user: User): UserResponse
    abstract fun userListToUserResponseList(users: List<User>): List<UserResponse>

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "createdAt", source = "createdAtIso")
    @Mapping(target = "role", source = "user.role", qualifiedByName = ["userRoleToAvro"])
    abstract fun userToUserCreatedEvent(user: User, createdAtIso: String): UserCreatedEvent


    // POST / CREATE
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "hashPassword", constant = "")
    @Mapping(target = "role", constant = "MEMBER")
    abstract fun userRequestToUser(userRequest: UserRequest): User

    @AfterMapping
    fun setPasswordAfterMapping(@MappingTarget user: User, userRequest: UserRequest) {
        user.hashPassword = passwordEncoder.encode(userRequest.password)
    }


    // PATCH / Update
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "hashPassword", ignore = true)
    @Mapping(source = "role", target = "role", qualifiedByName = ["updateRoleToUserRole"])
    abstract fun updateUserFromRequest(request: UserUpdateRequest, @MappingTarget user: User)

    @AfterMapping
    fun setPasswordAfterUpdate(@MappingTarget user: User, request: UserUpdateRequest) {
        if (request.password != null) {
            user.hashPassword = passwordEncoder.encode(request.password)
        }
    }

    @Named("userRoleToResponseRole")
    @ValueMappings(
        ValueMapping(source = "MEMBER", target = "member"),
        ValueMapping(source = "ADMIN", target = "admin")
    )
    fun mapUserRoleToResponseRole(role: UserRole): UserResponse.Role {
        return when (role) {
            UserRole.MEMBER -> UserResponse.Role.member
            UserRole.ADMIN -> UserResponse.Role.admin
        }
    }

    @Named("updateRoleToUserRole")
    @ValueMappings(
        ValueMapping(source = "member", target = "MEMBER"),
        ValueMapping(source = "admin", target = "ADMIN")
    )
    fun mapUpdateRoleToUserRole(role: UserUpdateRequest.Role?): UserRole? {
        return when (role) {
            UserUpdateRequest.Role.member -> UserRole.MEMBER
            UserUpdateRequest.Role.admin -> UserRole.ADMIN
            null -> null
        }
    }

    @Named("userRoleToAvro")
    fun mapUserRoleToAvro(role: UserRole): AvroUserRole =
        when (role) {
            UserRole.MEMBER -> AvroUserRole.MEMBER
            UserRole.ADMIN -> AvroUserRole.ADMIN
        }
}