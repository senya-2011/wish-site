package com.dabwish.dabwish.exception

open class AppException(message: String) : RuntimeException(message)

class UserNotFoundException(id: Long) : AppException("User with id:$id not found")

class UserAlreadyExistsException(name: String) : AppException("User with name $name already exists")

class InvalidPasswordException(msg: String) : AppException(msg)