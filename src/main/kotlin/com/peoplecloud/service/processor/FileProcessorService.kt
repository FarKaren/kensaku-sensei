package com.peoplecloud.service.processor

import com.peoplecloud.dto.processor.PicDataDto
import org.springframework.web.multipart.MultipartFile

interface FileProcessorService {

    fun processManualInput(input: String, tgtLang: String): List<PicDataDto>
    fun processFile(file: MultipartFile, tgtLang: String): List<PicDataDto>
}