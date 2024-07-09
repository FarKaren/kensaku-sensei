package com.peoplecloud.dto.language

import com.peoplecloud.models.DeeplLang

data class LanguageDto(
    val language: String,
    val isSupported: Boolean
)

fun DeeplLang.toLanguageDto(): LanguageDto {
    return LanguageDto(
        language = language!!,
        isSupported = isSupported!!
    )
}