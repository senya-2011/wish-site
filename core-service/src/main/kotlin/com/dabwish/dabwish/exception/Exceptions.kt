

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
class AlreadySubscribedException(subscriberId: Long, subscribedToId: Long) : AppException("You are already subscribed to it")
class NotSubscribedException(subscriberId: Long, subscribedToId: Long) : AppException("User $subscriberId is not subscribed to user $subscribedToId")