package com.dabwish.dabwish.exception

class UsernameNotFoundException(name: String) : AppException("User with name $name not found")

