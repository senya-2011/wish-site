package com.dabwish.dabwish.exception

class TelegramAlreadyVerifiedException(userId: Long) :
    AppException("Telegram already verified for user $userId")

