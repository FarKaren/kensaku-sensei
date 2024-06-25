package com.peoplecloud.controller

import com.peoplecloud.dto.exception.ErrorDto
import com.peoplecloud.dto.exception.ValidationErrorResponse
import com.peoplecloud.dto.exception.Violation
import com.peoplecloud.exceptions.UnsupportedFileType
import jakarta.validation.ConstraintViolationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ExceptionHandlerController: ResponseEntityExceptionHandler() {

    companion object {
        val log: Logger = LoggerFactory.getLogger(ExceptionHandlerController::class.java)
    }

    @ExceptionHandler(UnsupportedFileType::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    fun unsupportedFileTypeException(e: UnsupportedFileType): ResponseEntity<ErrorDto> {
        log.error(e.message)
        return ResponseEntity.ok(
            ErrorDto(
                errorCode = HttpStatus.BAD_REQUEST,
                errorMessage = e.message!!
            )
        )
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun internalError(e: Exception): ResponseEntity<ErrorDto> {
        log.error(e.message)
        return ResponseEntity.ok(
            ErrorDto(
                errorCode = HttpStatus.INTERNAL_SERVER_ERROR,
                errorMessage = e.message!!
            )
        )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    fun onConstraintViolationException(e: ConstraintViolationException): ValidationErrorResponse {
        log.error(e.message)
        val violations = e.constraintViolations
            .map {
                Violation(
                    fieldName = it.propertyPath.toString(),
                    message = it.message
                )
            }.toList()
        return ValidationErrorResponse(violations)
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        log.error(ex.message)
        val violations = ex.bindingResult.fieldErrors
            .map {
                Violation(
                    fieldName = it.field,
                    message = it.defaultMessage ?: ""
                )
            }.toList()
        return ResponseEntity.ok(ValidationErrorResponse(violations))
    }
}