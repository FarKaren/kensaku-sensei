package com.peoplecloud.dto.exception

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Validation error response")
data class ValidationErrorResponse(
    @Schema(description = "List of violations")
    val violations: List<Violation>
)
