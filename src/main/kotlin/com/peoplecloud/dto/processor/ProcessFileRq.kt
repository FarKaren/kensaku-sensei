package com.peoplecloud.dto.processor

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.web.multipart.MultipartFile

@Schema(description = "Request for processing files and manual input")
data class ProcessFileRq(
    @Schema(description = "List of files to be processed", required = true, example = "[{}]")
    val multipartFiles: List<MultipartFile>,
    @Schema(description = "Manual input for processing", example = "視;尖;剰;構逢")
    val manualInput: String?,
    @Schema(description = "Target language for processing", required = true, example = "Russia")
    val tgtLang: String
)
