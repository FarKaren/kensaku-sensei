package com.peoplecloud.controller

import com.deepl.api.QuotaExceededException
import com.deepl.api.TooManyRequestsException
import com.peoplecloud.dto.exception.ErrorDto
import com.peoplecloud.dto.exception.UnsupportedLanguageException
import com.peoplecloud.dto.exception.ValidationErrorResponse
import com.peoplecloud.dto.exception.Violation
import com.peoplecloud.exceptions.UnsupportedFileType
import jakarta.validation.ConstraintViolationException
import org.openqa.selenium.TimeoutException
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
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ExceptionHandlerController: ResponseEntityExceptionHandler() {

    companion object {
        val log: Logger = LoggerFactory.getLogger(ExceptionHandlerController::class.java)
        private const val MAX_UPLOAD_SIZE_EXCEEDED = "MAX UPLOAD SIZE EXCEEDED"
        private const val UNSUPPORTED_FILE_TYPE = "UNSUPPORTED_FILE_TYPE"
        private const val UNSUPPORTED_LANGUAGE = "UNSUPPORTED LANGUAGE"
        private const val TIMEOUT_EXCEPTION = "TIMEOUT EXCEPTION"
        private const val QUOTA_EXCEEDED_EXCEPTION = "QUOTA EXCEEDED EXCEPTION"
        private const val TOO_MANY_REQUEST = "TOO MANY REQUEST"
    }

    @ExceptionHandler(UnsupportedFileType::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    fun unsupportedFileTypeException(e: UnsupportedFileType): ResponseEntity<ErrorDto> {
        log.error("$UNSUPPORTED_FILE_TYPE: ${e.message}")
        return ResponseEntity.ok(
            ErrorDto(
                errorCode = HttpStatus.BAD_REQUEST.value(),
                errorMessage = "$UNSUPPORTED_FILE_TYPE: ${e.message}"
            )
        )
    }

    @ExceptionHandler(UnsupportedLanguageException::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    fun unsupportedLanguageException(e: UnsupportedLanguageException): ResponseEntity<ErrorDto> {
        log.error("$UNSUPPORTED_LANGUAGE: ${e.message}")
        return ResponseEntity.ok(
            ErrorDto(
                errorCode = HttpStatus.BAD_REQUEST.value(),
                errorMessage = "$UNSUPPORTED_LANGUAGE: ${e.message}"
            )
        )
    }

    @ExceptionHandler(MaxUploadSizeExceededException::class)
    @ResponseStatus(value = HttpStatus.PAYLOAD_TOO_LARGE)
    fun maxUploadSizeExceededException(e: MaxUploadSizeExceededException): ResponseEntity<ErrorDto> {
        log.error("$MAX_UPLOAD_SIZE_EXCEEDED: ${e.message}")
        return ResponseEntity.ok(
            ErrorDto(
                errorCode = HttpStatus.PAYLOAD_TOO_LARGE.value(),
                errorMessage = "$MAX_UPLOAD_SIZE_EXCEEDED: ${e.message}"
            )
        )
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    fun internalError(e: Exception): ResponseEntity<ErrorDto> {
        log.error(e.message)
        return ResponseEntity.ok(
            ErrorDto(
                errorCode = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                errorMessage = e.message!!
            )
        )
    }

    @ExceptionHandler(TimeoutException::class)
    @ResponseStatus(value = HttpStatus.REQUEST_TIMEOUT)
    fun seleniumTimeoutException(e: TimeoutException): ResponseEntity<ErrorDto> {
        log.error(e.message)
        return ResponseEntity.ok(
            ErrorDto(
                errorCode = HttpStatus.REQUEST_TIMEOUT.value(),
                errorMessage = "$TIMEOUT_EXCEPTION: ${e.message}"
            )
        )
    }

    @ExceptionHandler(QuotaExceededException::class)
    fun deeplQuotaExceededException(e: QuotaExceededException): ResponseEntity<ErrorDto> {
        log.error(e.message)
        return ResponseEntity.ok(
            ErrorDto(
                errorCode = 456,
                errorMessage = "$QUOTA_EXCEEDED_EXCEPTION: ${e.message}"
            )
        )
    }

    @ExceptionHandler(TooManyRequestsException::class)
    @ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS)
    fun deeplTooManyRequestsException(e: TooManyRequestsException): ResponseEntity<ErrorDto> {
        log.error(e.message)
        return ResponseEntity.ok(
            ErrorDto(
                errorCode = HttpStatus.TOO_MANY_REQUESTS.value(),
                errorMessage = "$TOO_MANY_REQUEST: ${e.message}"
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