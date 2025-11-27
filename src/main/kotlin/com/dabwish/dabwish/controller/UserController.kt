package com.dabwish.dabwish.controller

import com.dabwish.dabwish.data.CreateUser
import com.dabwish.dabwish.data.User
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController {

    @GetMapping()
    fun getUsers(): Iterable<User> =
        listOf(User(1, "User1"), User(2, "User2"))

    @PostMapping
    fun create(@RequestBody request: CreateUser): User =
        User(3, request.name)
}