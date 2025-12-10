package com.dabwish.dabwish.service

import com.dabwish.dabwish.config.MinioProperties
import com.dabwish.dabwish.exception.FileStorageException
import io.github.oshai.kotlinlogging.KotlinLogging
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.util.UUID

private val logger = KotlinLogging.logger {}


@Service
class MinioService(
    private val minioClient: MinioClient,
    private val minioProperties: MinioProperties,
    @Value("\${minio.public-url:}") private val minioPublicUrl: String?,
) {
    fun uploadFile(file: MultipartFile, prefix: String = ""): String {
        val originalFilename = file.originalFilename
            ?: throw FileStorageException("File must have a name")
        
        if (file.isEmpty) {
            throw FileStorageException("File cannot be empty")
        }

        val contentType = file.contentType ?: "application/octet-stream"
        val extension = extractExtension(originalFilename)

        return uploadToMinio(file.inputStream, file.size, contentType, prefix, extension)
    }

    fun deleteFile(objectName: String) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(minioProperties.bucketName)
                    .`object`(objectName)
                    .build()
            )
            logger.info { "Deleted file from MinIO: $objectName" }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to delete file from MinIO: $objectName" }
        }
    }

    fun getFileUrl(objectName: String): String {
        val cleanUrl = minioProperties.url.removeSuffix("/")
        return "$cleanUrl/${minioProperties.bucketName}/$objectName"
        return "$cleanUrl/${minioProperties.bucketName}/$objectName"
    }

    fun toPublicUrl(url: String?): String? {
        if (url.isNullOrBlank()) return url
        val internalBase = minioProperties.url.removeSuffix("/")
        val publicBase = resolvePublicBase().removeSuffix("/")
        return if (url.startsWith(internalBase)) {
            publicBase + url.removePrefix(internalBase)
        } else {
            url
        }
    }

    fun extractObjectNameFromUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        
        val bucketPath = "${minioProperties.bucketName}/"
        return if (url.contains(bucketPath)) {
            url.substringAfter(bucketPath)
        } else {
            null
        }
    }

    private fun uploadToMinio(
        stream: InputStream,
        size: Long,
        contentType: String,
        prefix: String,
        extension: String,
    ): String {
        val objectName = buildObjectName(prefix, extension)

        try {
            stream.use {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(minioProperties.bucketName)
                        .`object`(objectName)
                        .stream(it, size, -1)
                        .contentType(contentType)
                        .build()
                )
            }
            logger.info { "Successfully uploaded file to MinIO: $objectName" }
            return objectName
        } catch (e: Exception) {
            logger.error(e) { "Failed to upload file to MinIO. Bucket: ${minioProperties.bucketName}, Object: $objectName" }
            throw FileStorageException("Failed to upload file to storage", e)
        }
    }

    private fun buildObjectName(prefix: String, extension: String): String {
        return "${prefix}${UUID.randomUUID()}.$extension"
    }

    private fun extractExtension(filename: String): String {
        return filename.substringAfterLast('.', "")
            .takeIf { it.isNotBlank() && it.length <= 5 }
            ?: "bin"
    }

    private fun resolvePublicBase(): String =
        minioPublicUrl?.takeIf { it.isNotBlank() } ?: minioProperties.url
}
