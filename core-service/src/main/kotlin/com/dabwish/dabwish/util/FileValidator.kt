package com.dabwish.dabwish.util

import com.dabwish.dabwish.exception.FileSizeLimitExceededException
import com.dabwish.dabwish.exception.InvalidFileFormatException
import org.springframework.web.multipart.MultipartFile
import org.springframework.http.MediaType

object FileValidator {
    private const val MAX_SIZE = 10 * 1024 * 1024 // 10 MB
    private val ALLOWED_CONTENT_TYPES = setOf(
        MediaType.IMAGE_JPEG_VALUE,
        MediaType.IMAGE_PNG_VALUE,
        "image/webp"
    )

    fun validateImage(file: MultipartFile) {
        if (file.isEmpty) return

        if (file.size > MAX_SIZE) {
            throw FileSizeLimitExceededException()
        }

        val contentType = file.contentType ?: ""
        if (contentType !in ALLOWED_CONTENT_TYPES) {
            throw InvalidFileFormatException()
        }
    }
}