package com.dabwish.dabwish.controller

import com.dabwish.dabwish.exception.InvalidCredentialsException
import com.dabwish.dabwish.exception.MissingCreatedAtException
import com.dabwish.dabwish.exception.UserAlreadyExistsException
import com.dabwish.dabwish.exception.UsernameNotFoundException
import com.dabwish.dabwish.exception.UserNotFoundException
import com.dabwish.dabwish.exception.WishNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import com.dabwish.dabwish.generated.dto.Error

@RestControllerAdvice
class GlobalExceptionHandler {


    @ExceptionHandler(UserNotFoundException::class)
    fun handleNotFound(e: UserNotFoundException): ResponseEntity<Error> {

        val errorResponse = Error(
            code = HttpStatus.NOT_FOUND.value(),
            message = e.message ?: "Resource not found"
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(WishNotFoundException::class)
    fun handleNotFound(e: WishNotFoundException): ResponseEntity<Error> {
    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUsernameNotFound(e: UsernameNotFoundException): ResponseEntity<Error> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            Error(
                code = HttpStatus.NOT_FOUND.value(),
                message = e.message ?: "User not found",
            ),
        )


        val errorResponse = Error(
            code = HttpStatus.NOT_FOUND.value(),
            message = e.message ?: "Resource not found"
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(MissingCreatedAtException::class)
    fun handleMissingCreatedAt(e: MissingCreatedAtException): ResponseEntity<Error> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                Error(
                    code = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    message = e.message ?: "Missing createdAt",
                ),
            )

    @ExceptionHandler(UserAlreadyExistsException::class)
    fun handleConflict(e: UserAlreadyExistsException): ResponseEntity<Error> {
        val errorResponse = Error(
            code = HttpStatus.CONFLICT.value(),
            message = e.message ?: "Conflict"
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleConflict(e: InvalidCredentialsException): ResponseEntity<Error> {
        val errorResponse = Error(
            code = HttpStatus.CONFLICT.value(),
            message = e.message ?: "Conflict"
        )
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<Error> {
        val errors = e.bindingResult.fieldErrors.joinToString(", ") {
            "${it.field}: ${it.defaultMessage}"
        }

        val errorResponse = Error(
            code = HttpStatus.BAD_REQUEST.value(),
            message = "Validation failed: $errors"
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }



    @ExceptionHandler(Exception::class)
    fun handleGeneral(e: Exception): ResponseEntity<Error> {
        val errorResponse = Error(
            code = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "${e}",
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}