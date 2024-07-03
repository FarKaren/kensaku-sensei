package com.peoplecloud.dto

data class PicDataDto(
    val sourceWord: String,
    val targetWord: String,
    var urls: List<String> = emptyList()
)
