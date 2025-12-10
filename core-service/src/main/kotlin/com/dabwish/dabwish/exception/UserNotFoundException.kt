package com.dabwish.dabwish.exception

class UserNotFoundException(id: Long) : AppException("User with id:$id not found")

