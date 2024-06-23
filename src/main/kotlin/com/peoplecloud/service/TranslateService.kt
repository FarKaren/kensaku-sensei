package com.peoplecloud.service

import com.itextpdf.kernel.pdf.PdfDocument
import com.peoplecloud.dto.PageTranslation
import org.springframework.web.multipart.MultipartFile

interface TranslateService {

    fun translateAndProcessPdf(multipartFile: MultipartFile, srcLang: String, tgtLang: String): String
}