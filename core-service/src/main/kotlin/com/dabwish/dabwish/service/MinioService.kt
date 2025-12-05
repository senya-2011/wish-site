package com.dabwish.dabwish.service

import com.dabwish.dabwish.exception.FileStorageException
import io.minio.MinioClient
import io.minio.PutObjectArgs
import io.minio.RemoveObjectArgs
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream
import java.util.UUID


@Service
class MinioService(
    private val minioClient: MinioClient,
    @Value("\${minio.bucket-name}") private val bucketName: String,
    @Value("\${minio.url}") private val minioUrl: String,
    @Value("\${minio.public-url:}") private val minioPublicUrl: String?,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

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
                    .bucket(bucketName)
                    .`object`(objectName)
                    .build()
            )
            logger.info("Deleted file from MinIO: $objectName")
        } catch (e: Exception) {
            logger.warn("Failed to delete file from MinIO: $objectName", e)
        }
    }

    fun getFileUrl(objectName: String): String {
        val cleanUrl = resolvePublicBase().removeSuffix("/")
        return "$cleanUrl/$bucketName/$objectName"
    }

    fun toPublicUrl(url: String?): String? {
        if (url.isNullOrBlank()) return url
        val internalBase = minioUrl.removeSuffix("/")
        val publicBase = resolvePublicBase().removeSuffix("/")
        return if (url.startsWith(internalBase)) {
            publicBase + url.removePrefix(internalBase)
        } else {
            url
        }
    }

    fun extractObjectNameFromUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        
        val bucketPath = "$bucketName/"
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
                        .bucket(bucketName)
                        .`object`(objectName)
                        .stream(it, size, -1)
                        .contentType(contentType)
                        .build()
                )
            }
            logger.info("Successfully uploaded file to MinIO: $objectName")
            return objectName
        } catch (e: Exception) {
            logger.error("Failed to upload file to MinIO. Bucket: $bucketName, Object: $objectName", e)
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
        minioPublicUrl?.takeIf { it.isNotBlank() } ?: minioUrl
}
