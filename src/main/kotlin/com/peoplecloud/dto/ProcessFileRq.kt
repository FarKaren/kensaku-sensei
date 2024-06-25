package com.peoplecloud.dto

import org.springframework.web.multipart.MultipartFile

data class ProcessFileRq(
    val multipartFiles: List<MultipartFile>,
    val srcLang: String,
    val tgtLang: String
)
