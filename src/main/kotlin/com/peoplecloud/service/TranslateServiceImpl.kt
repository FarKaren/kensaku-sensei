package com.peoplecloud.service

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.canvas.parser.PdfDocumentContentParser
import com.itextpdf.kernel.pdf.canvas.parser.listener.ITextExtractionStrategy
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy
import com.peoplecloud.client.LibretranslateClient
import com.peoplecloud.client.LibretranslateRq
import com.peoplecloud.dto.PageTranslation
import org.springframework.stereotype.Service

@Service
class TranslateServiceImpl(
    private val translateClient: LibretranslateClient
): TranslateService{

    override fun processAndTranslatePdf(pdfDocument: PdfDocument, srcLang: String, tgtLang: String): List<PageTranslation> {
        val translations = mutableListOf<PageTranslation>()

        val parser = PdfDocumentContentParser(pdfDocument)
        val strategy: ITextExtractionStrategy = SimpleTextExtractionStrategy()

        for (pageNumber in 1..pdfDocument.numberOfPages) {
            val pageText = parser.processContent(pageNumber, strategy).resultantText
            val translation = translateText(pageText, srcLang, tgtLang)
            translations.add(PageTranslation(pageNumber, translation))
        }

        return translations
    }

    private fun translateText(text: String, srcLang: String, tgtLang: String): String {
        val translationRequest = LibretranslateRq(q = text, source = srcLang, target = tgtLang)
        val translationResponse = translateClient.translate(translationRequest)
        return translationResponse.translatedText
    }
}