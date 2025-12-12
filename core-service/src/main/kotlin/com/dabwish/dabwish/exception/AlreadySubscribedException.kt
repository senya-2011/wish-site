package com.dabwish.dabwish.exception

class AlreadySubscribedException(subscriberId: Long, subscribedToId: Long) :
    AppException("You are already subscribed to it")

