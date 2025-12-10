package com.dabwish.dabwish.exception

class MissingCreatedAtException(userId: Long) :
    AppException("User $userId has null createdAt after persistence")

