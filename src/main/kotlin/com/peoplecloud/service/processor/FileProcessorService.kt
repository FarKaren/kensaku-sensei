package com.peoplecloud.service.processor

import com.peoplecloud.dto.PicDataDto
import org.springframework.web.multipart.MultipartFile

interface FileProcessorService {

    fun processFile(file: MultipartFile, srcLang: String, tgtLang: String): List<PicDataDto>
}