package com.peoplecloud.dto.exception

data class ValidationErrorResponse(
    val violations: List<Violation>
)
