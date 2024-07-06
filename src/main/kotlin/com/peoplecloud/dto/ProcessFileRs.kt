package com.peoplecloud.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response for processed files and manual input")
data class ProcessFileRs(
    @Schema(description = "List of processed files", example = "[{\"data\":[{\"sourceWord\":\"test\",\"targetWord\":\"тест\",\"urls\":[\"http://example.com\"]}]}]")
    val files: List<FileDataDto>,
    @Schema(description = "Result of manual input processing", example = "[{\"sourceWord\":\"example\",\"targetWord\":\"пример\",\"urls\":[]}]")
    val manualInput: List<PicDataDto>
)



