package com.dabwish.dabwish.mapper

import com.dabwish.dabwish.dto.user.ViewUser
import com.dabwish.dabwish.model.user.User
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")
interface UserMapper {
    fun userToViewUser(user: User): ViewUser
    fun viewUserToUser(userViewUser: ViewUser): User
}