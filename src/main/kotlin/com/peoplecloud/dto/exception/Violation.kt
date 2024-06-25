package com.peoplecloud.dto.exception

data class Violation(
    val fieldName: String,
    val message: String
)