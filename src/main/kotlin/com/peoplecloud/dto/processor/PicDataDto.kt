package com.peoplecloud.dto.processor

import io.swagger.v3.oas.annotations.media.Schema


@Schema(description = "Picture data")
data class PicDataDto(
    @Schema(description = "Source word", example = "word")
    val sourceWord: String,
    @Schema(description = "Target word", example = "слово")
    val targetWord: String,
    @Schema(description = "List of URLs", example = "[\"http://example.com\"]")
    var urls: List<String> = emptyList()
)
