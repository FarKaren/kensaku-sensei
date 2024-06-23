package com.peoplecloud.dto

import org.springframework.web.multipart.MultipartFile

data class TranslateRq(
    val multipartFiles: List<MultipartFile>,
    val srcLang: String,
    val tgtLang: String
)
