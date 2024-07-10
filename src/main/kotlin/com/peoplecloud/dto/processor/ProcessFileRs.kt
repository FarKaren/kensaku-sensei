package com.peoplecloud.dto.processor

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response for processed files and manual input")
data class ProcessFileRs(
    @Schema(description = "List of processed files", example = "[{\"data\":[{\"wordId\":\"1\", \"sourceWord\":\"test\",\"targetWord\":\"тест\",\"urls\":[\"http://example.com\"]}]}]")
    val files: List<FileDataDto>,
    @Schema(description = "Result of manual input processing", example = "[{\"wordId\":\"1\", \"sourceWord\":\"example\",\"targetWord\":\"пример\",\"urls\":[]}]")
    val manualInput: List<PicDataDto>
)



