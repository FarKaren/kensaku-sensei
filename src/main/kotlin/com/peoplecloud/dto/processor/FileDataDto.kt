package com.peoplecloud.dto.processor

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Data of processed file")
data class FileDataDto(
    @Schema(description = "List of picture data", example = "[{\"sourceWord\":\"word\",\"targetWord\":\"слово\",\"urls\":[\"http://example.com\"]}]")
    val data: List<PicDataDto>
)
