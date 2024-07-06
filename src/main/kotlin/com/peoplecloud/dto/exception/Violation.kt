package com.peoplecloud.dto.exception

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Details of a validation error")
data class Violation(
    @Schema(description = "Field name", example = "fieldName")
    val fieldName: String,
    @Schema(description = "Error message", example = "must not be blank")
    val message: String
)