package com.peoplecloud.service

import com.itextpdf.kernel.pdf.PdfDocument
import com.peoplecloud.dto.PageTranslation

interface TranslateService {

    fun processAndTranslatePdf(pdfDocument: PdfDocument, srcLang: String, tgtLang: String): List<PageTranslation>
}