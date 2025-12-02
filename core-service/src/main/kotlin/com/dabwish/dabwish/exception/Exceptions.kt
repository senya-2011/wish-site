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