package com.peoplecloud.controller


import com.peoplecloud.dto.FileDataDto
import com.peoplecloud.dto.ProcessFileRq
import com.peoplecloud.dto.ProcessFileRs
import com.peoplecloud.service.processor.FileProcessorService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1")
class FileProcessorController(
    private val fileProcessorService: FileProcessorService
) {

    @PostMapping("/analyzer")
    fun analyze(@ModelAttribute request: ProcessFileRq): ResponseEntity<ProcessFileRs> {
        val fileProcessResults = mutableListOf<FileDataDto>()

        for (file in request.multipartFiles) {
            if (file.isEmpty) continue
            val pictureData = fileProcessorService.processFile(file, request.tgtLang)
            fileProcessResults.add(FileDataDto(pictureData))
        }
        val inputResult =
            if(!request.manualInput.isNullOrEmpty()) {
                fileProcessorService.processManualInput(request.manualInput, request.tgtLang)
            } else emptyList()

        return ResponseEntity.ok(ProcessFileRs(fileProcessResults, inputResult))
    }
}
