package com.peoplecloud.controller

import com.peoplecloud.dto.exception.ErrorDto
import com.peoplecloud.dto.language.LanguageListRs
import com.peoplecloud.service.language.LanguagesService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/language")
class LanguageController(
    private val languagesService: LanguagesService
) {

    @Operation(
        summary = "Available languages",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful Operation",
                content = [Content(schema = Schema(implementation = LanguageListRs::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request or UNSUPPORTED FILE TYPE or UNSUPPORTED LANGUAGE",
                content = [Content(schema = Schema(implementation = ErrorDto::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = ErrorDto::class))]
            )
        ]
    )
    @GetMapping
    fun languages(): ResponseEntity<LanguageListRs> {
        val result = languagesService.languages()
        return ResponseEntity.ok(result)
    }
}