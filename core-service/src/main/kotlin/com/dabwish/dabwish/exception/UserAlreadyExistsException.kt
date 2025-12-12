package com.dabwish.dabwish.exception

class UserAlreadyExistsException(name: String) : AppException("User with name $name already exists")

