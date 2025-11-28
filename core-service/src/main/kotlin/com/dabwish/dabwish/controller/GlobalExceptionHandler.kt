package com.dabwish.dabwish.controller

import com.dabwish.dabwish.exception.UserAlreadyExistsException
import com.dabwish.dabwish.exception.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import com.dabwish.dabwish.generated.dto.Error

@RestControllerAdvice
class GlobalExceptionHandler {


    // 1. Обработка "Не найдено" -> 404
    @ExceptionHandler(UserNotFoundException::class)
    fun handleNotFound(e: UserNotFoundException): ResponseEntity<Error> {

        val errorResponse = Error(
            code = HttpStatus.NOT_FOUND.value(),
            message = e.message ?: "Resource not found"
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    // 2. Обработка "Уже существует" -> 409 Conflict
    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleConflict(e: UserAlreadyExistsException): ResponseEntity<Error> {
        val errorResponse = Error(
            code = HttpStatus.CONFLICT.value(),
            message = e.message ?: "Conflict"
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    // 3. Обработка ошибок валидации (@Valid) -> 400
    // Это сработает, если клиент пришлет пустой name или кривой email
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<Error> {
        // Собираем все ошибки полей в одну строку
        val errors = e.bindingResult.fieldErrors.joinToString(", ") {
            "${it.field}: ${it.defaultMessage}"
        }

        val errorResponse = Error(
            code = HttpStatus.BAD_REQUEST.value(),
            message = "Validation failed: $errors"
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    // 4. Обработка всего остального (500 Internal Server Error)
    // Чтобы клиент не видел страшные Java стэк-трейсы
    @ExceptionHandler(Exception::class)
    fun handleGeneral(e: Exception): ResponseEntity<Error> {
        val errorResponse = Error(
            code = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "Internal Server Error. Please contact support."
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}