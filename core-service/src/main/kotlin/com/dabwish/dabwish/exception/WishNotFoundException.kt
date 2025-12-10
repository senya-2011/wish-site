package com.dabwish.dabwish.exception

class WishNotFoundException(id: Long) : AppException("Wish with id:$id not found")

