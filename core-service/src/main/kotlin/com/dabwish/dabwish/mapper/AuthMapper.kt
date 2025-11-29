package com.dabwish.dabwish.mapper

import com.dabwish.dabwish.generated.dto.LoginResponse
import com.dabwish.dabwish.generated.dto.RegisterRequest
import com.dabwish.dabwish.model.user.User
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.NullValuePropertyMappingStrategy
import org.springframework.beans.factory.annotation.Autowired

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
)
abstract class AuthMapper {

    @Autowired
    protected lateinit var userMapper: UserMapper

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hashPassword", ignore = true)
    @Mapping(target = "role", constant = "MEMBER")
    @Mapping(target = "createdAt", expression = "java(java.time.OffsetDateTime.now())")
    @Mapping(target = "updatedAt", ignore = true)
    abstract fun registerRequestToUser(request: RegisterRequest): User

    fun toLoginResponse(
        user: User,
        accessToken: String,
        expiresInSeconds: Long,
        tokenType: String = "Bearer",
    ): LoginResponse {
        return LoginResponse(
            accessToken = accessToken,
            tokenType = tokenType,
            expiresIn = expiresInSeconds,
            user = userMapper.userToUserResponse(user),
        )
    }
}