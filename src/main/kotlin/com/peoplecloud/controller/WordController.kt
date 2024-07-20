package com.peoplecloud.controller

import com.peoplecloud.dto.exception.ErrorDto
import com.peoplecloud.dto.language.LanguageListRs
import com.peoplecloud.dto.word.WordCardRs
import com.peoplecloud.models.Languages
import com.peoplecloud.service.WordService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/word")
class WordController(
    private val wordService: WordService
) {

    @Operation(
        summary = "Available languages",
        parameters = [
            Parameter(
                name = "wordId",
                `in` = ParameterIn.QUERY,
                description = "Идентификатор слова",
                required = true,
                schema = Schema(type = "integer", format = "int64")
            ),
            Parameter(
                name = "language",
                `in` = ParameterIn.QUERY,
                description = "Язык",
                required = true,
                schema = Schema(implementation = Languages::class)
            )
        ],
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
    @GetMapping("/card")
    fun getWordCard(
        @RequestParam wordId: Long,
        @RequestParam language: Languages
    ): ResponseEntity<WordCardRs> {
        val result = wordService.getWordCard(wordId, language)
        return ResponseEntity.ok(WordCardRs(result))
    }
}