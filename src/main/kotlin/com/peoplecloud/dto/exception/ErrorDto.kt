package com.peoplecloud.dto.exception

import org.springframework.http.HttpStatusCode

data class ErrorDto(
    val errorCode: HttpStatusCode,
    val errorMessage: String
)




