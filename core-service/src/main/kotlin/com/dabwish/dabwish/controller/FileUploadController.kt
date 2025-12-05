package com.dabwish.dabwish.controller

import com.dabwish.dabwish.generated.dto.FileUploadResponse
import com.dabwish.dabwish.service.MinioService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api")
@Tag(name = "Files", description = "Управление файлами")
class FileUploadController(
    private val minioService: MinioService,
) {

    @PostMapping("/files/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "Загрузить файл (фото) в MinIO",
        description = "Загружает файл в MinIO и возвращает URL для доступа к файлу",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponse(
        responseCode = "200",
        description = "Файл успешно загружен",
        content = [Content(schema = Schema(implementation = FileUploadResponse::class))]
    )
    @ApiResponse(responseCode = "400", description = "Ошибка валидации (файл не предоставлен или неверный формат)")
    @ApiResponse(responseCode = "500", description = "Ошибка при загрузке файла в MinIO")
    fun uploadFile(@RequestPart("file") file: MultipartFile): ResponseEntity<FileUploadResponse> {
        if (file.isEmpty) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }

        val contentType = file.contentType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE

        if (!contentType.startsWith("image/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }

        val maxSize = 10 * 1024 * 1024L
        if (file.size > maxSize) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }

        val objectName = minioService.uploadFile(file, prefix = "wishes/")
        val fileUrl = minioService.getFileUrl(objectName)

        val response = FileUploadResponse(
            fileUrl = fileUrl,
            objectName = objectName
        )

        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/files/{objectName:.*}")
    @Operation(
        summary = "Удалить файл из MinIO",
        description = "Удаляет файл из MinIO по его object name",
        security = [SecurityRequirement(name = "bearerAuth")]
    )
    @ApiResponse(responseCode = "200", description = "Файл успешно удален")
    @ApiResponse(responseCode = "400", description = "Некорректный object name")
    fun deleteFile(
        @Parameter(description = "Object name файла в MinIO (например, wishes/uuid.jpg)")
        @PathVariable objectName: String
    ): ResponseEntity<Unit> {
        if (objectName.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }

        minioService.deleteFile(objectName)
        return ResponseEntity.ok().build()
    }
}


