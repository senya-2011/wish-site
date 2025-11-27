package com.dabwish.dabwish.controller

import com.dabwish.dabwish.model.user.User
import com.dabwish.dabwish.repository.UserRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    val userRepository: UserRepository
) {
    @GetMapping()
    fun getUsers(): Iterable<User> =
        listOf(User(1, "User1"), User(2, "User2"))

}