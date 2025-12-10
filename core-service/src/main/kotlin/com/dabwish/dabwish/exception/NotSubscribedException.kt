package com.dabwish.dabwish.exception

class NotSubscribedException(subscriberId: Long, subscribedToId: Long) :
    AppException("User $subscriberId is not subscribed to user $subscribedToId")

