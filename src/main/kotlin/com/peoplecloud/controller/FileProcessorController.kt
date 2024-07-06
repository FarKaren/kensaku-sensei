package com.peoplecloud.controller


import com.peoplecloud.dto.FileDataDto
import com.peoplecloud.dto.ProcessFileRq
import com.peoplecloud.dto.ProcessFileRs
import com.peoplecloud.dto.exception.ErrorDto
import com.peoplecloud.service.processor.FileProcessorService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1")
class FileProcessorController(
    private val fileProcessorService: FileProcessorService
) {

    @Operation(
        summary = "Analyze files and manual input",
        requestBody = RequestBody(
            description = "Request body containing files and manual input data",
            required = true,
            content = [Content(schema = Schema(implementation = ProcessFileRq::class))]
        ),
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successful Operation",
                content = [Content(schema = Schema(implementation = ProcessFileRs::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request or unsupported file type/language",
                content = [Content(schema = Schema(implementation = ErrorDto::class))]
            ),
            ApiResponse(
                responseCode = "413",
                description = "Uploaded file size exceeds the limit",
                content = [Content(schema = Schema(implementation = ErrorDto::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = ErrorDto::class))]
            ),
            ApiResponse(
                responseCode = "408",
                description = "Request timeout",
                content = [Content(schema = Schema(implementation = ErrorDto::class))]
            ),
            ApiResponse(
                responseCode = "456",
                description = "Quota exceeded",
                content = [Content(schema = Schema(implementation = ErrorDto::class))]
            ),
            ApiResponse(
                responseCode = "429",
                description = "Too many requests",
                content = [Content(schema = Schema(implementation = ErrorDto::class))]
            )
        ]
    )
    @PostMapping("/analyzer")
    fun analyze(@ModelAttribute request: ProcessFileRq): ResponseEntity<ProcessFileRs> {
        val fileProcessResults = mutableListOf<FileDataDto>()

        for (file in request.multipartFiles) {
            if (file.isEmpty) continue
            val pictureData = fileProcessorService.processFile(file, request.tgtLang)
            fileProcessResults.add(FileDataDto(pictureData))
        }
        val inputResult =
            if (!request.manualInput.isNullOrEmpty()) {
                fileProcessorService.processManualInput(request.manualInput, request.tgtLang)
            } else emptyList()

        return ResponseEntity.ok(ProcessFileRs(fileProcessResults, inputResult))
    }
}
