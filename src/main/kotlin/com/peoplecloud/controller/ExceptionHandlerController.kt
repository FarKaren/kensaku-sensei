package com.peoplecloud.controller

import com.deepl.api.QuotaExceededException
import com.deepl.api.TooManyRequestsException
import com.peoplecloud.dto.exception.ErrorDto
import com.peoplecloud.dto.exception.ValidationErrorResponse
import com.peoplecloud.dto.exception.Violation
import com.peoplecloud.exceptions.EntityNotFoundException
import com.peoplecloud.exceptions.UnsupportedFileType
import com.peoplecloud.exceptions.UnsupportedLanguageException
import com.peoplecloud.exceptions.YandexDictionaryClientException
import jakarta.validation.ConstraintViolationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
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
        private const val UNSUPPORTED_FILE_TYPE = "UNSUPPORTED FILE TYPE"
        private const val UNSUPPORTED_LANGUAGE = "UNSUPPORTED LANGUAGE"
        private const val TIMEOUT_EXCEPTION = "TIMEOUT EXCEPTION"
        private const val QUOTA_EXCEEDED_EXCEPTION = "QUOTA EXCEEDED EXCEPTION"
        private const val TOO_MANY_REQUEST = "TOO MANY REQUEST"
        private const val ENTITY_NOT_FOUND_EXCEPTION = "ENTITY NOT FOUND EXCEPTION"
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

    @ExceptionHandler(YandexDictionaryClientException::class)
    fun yandexDictionaryClientException(e: YandexDictionaryClientException): ResponseEntity<ErrorDto> {
        log.error(e.message)
        return ResponseEntity.ok(
            ErrorDto(
                errorCode = 418,
                errorMessage = e.message!!
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

    @ExceptionHandler(EntityNotFoundException::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    fun entityNotFoundException(e: EntityNotFoundException): ResponseEntity<ErrorDto> {
        log.error(e.message)
        return ResponseEntity.ok(
            ErrorDto(
                errorCode = HttpStatus.BAD_REQUEST.value(),
                errorMessage = "$ENTITY_NOT_FOUND_EXCEPTION: ${e.message}"
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

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        log.error("Input error: {}", ex.message)

        val errorDto = ErrorDto(
            errorCode = HttpStatus.BAD_REQUEST.value(),
            errorMessage = "Malformed JSON request: ${ex.message}"
        )

        return ResponseEntity(errorDto, HttpStatus.BAD_REQUEST)
    }

    override fun handleMissingServletRequestParameter(
        ex: MissingServletRequestParameterException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        log.error(ex.message)
        val violation = Violation(
            fieldName = ex.parameterName,
            message = ex.message
        )
        return ResponseEntity.ok(ValidationErrorResponse(listOf(violation)))
    }


}