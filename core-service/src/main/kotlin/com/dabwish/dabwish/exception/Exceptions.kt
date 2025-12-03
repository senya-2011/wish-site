package com.dabwish.dabwish.exception

open class AppException(message: String) : RuntimeException(message)

class UserNotFoundException(id: Long) : AppException("User with id:$id not found")

class UsernameNotFoundException(name: String) : AppException("User with name $name not found")

class UserAlreadyExistsException(name: String) : AppException("User with name $name already exists")

class InvalidCredentialsException(msg: String) : AppException(msg)

class MissingCreatedAtException(userId: Long) :
    AppException("User $userId has null createdAt after persistence")


// Wishes
class WishNotFoundException(id: Long) : AppException("Wish with id:$id not found")

// MinIO
class FileStorageException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class InvalidFileFormatException(): AppException("Only images (JPEG, PNG, WEBP) are allowed")

class FileSizeLimitExceededException(): AppException("File size exceeds 10MB limit")

// Telegram Verification
class TelegramAlreadyVerifiedException(userId: Long) : AppException("Telegram already verified for user $userId")
class TelegramVerificationCodeExpiredException() : AppException("Verification code has expired")
class TelegramVerificationCodeInvalidException() : AppException("Invalid verification code")

// Subscriptions
class CannotSubscribeToSelfException() : AppException("Cannot subscribe to yourself")
class AlreadySubscribedException(subscriberId: Long, subscribedToId: Long) : AppException("User $subscriberId is already subscribed to user $subscribedToId")
class NotSubscribedException(subscriberId: Long, subscribedToId: Long) : AppException("User $subscriberId is not subscribed to user $subscribedToId")