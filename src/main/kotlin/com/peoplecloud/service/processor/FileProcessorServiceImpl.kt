package com.peoplecloud.service.processor

import com.peoplecloud.client.LibretranslateClient
import com.peoplecloud.client.LibretranslateRq
import com.peoplecloud.dto.PicDataDto
import com.peoplecloud.exceptions.UnsupportedFileType
import com.peoplecloud.service.analyzer.AnalyzerService
import com.peoplecloud.service.findare.PictureFinder
import net.sourceforge.tess4j.Tesseract
import net.sourceforge.tess4j.TesseractException
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.text.PDFTextStripper
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.awt.image.RescaleOp
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO

@Service
class FileProcessorServiceImpl(
    private val translateClient: LibretranslateClient,
    private val analyzerService: AnalyzerService,
    private val pictureFinder: PictureFinder
) : FileProcessorService {

    // Путь к каталогу tessdata
    private val tessDataPath = "/usr/share/tesseract-ocr/4.00/tessdata/"

    // Инициализация Tesseract OCR
    private val tesseract = Tesseract().apply {
        setDatapath(tessDataPath) // Укажите путь к tessdata
        setVariable("tessedit_ocr_engine_mode", "3") // OCR Engine mode: 3 - Default, based on what is available
        setVariable("textord_heavy_nr", "1") // Enables automatic orientation correction
        setVariable("textord_straight_baselines", "1") // Assists in correct text detection
    }

//    override fun translateAndProcessPdf(pdfDocument: PdfDocument, srcLang: String, tgtLang: String): List<String> {
//        val translations: StringBuilder = StringBuilder()
//
//        val parser = PdfDocumentContentParser(pdfDocument)
//        val strategy: ITextExtractionStrategy = SimpleTextExtractionStrategy()
//
//        for (pageNumber in 1..pdfDocument.numberOfPages) {
//            val pageText = parser.processContent(pageNumber, strategy).resultantText
//            val translation = translateText(pageText, srcLang, tgtLang)
//            translations.append(translation)
//        }
//        return this.analyzeText(translations, srcLang, tgtLang)
//    }

    override fun processFile(file: MultipartFile, srcLang: String, tgtLang: String): List<PicDataDto> {
        return when (getExtension(file.originalFilename)) {
            "pdf" -> processPdf(file, srcLang, tgtLang)
            "jpeg", "jpg", "png" -> processImage(file, srcLang, tgtLang)
            else -> throw UnsupportedFileType("Unsupported file type: ${file.originalFilename}")
        }

    }

    private fun getExtension(filename: String?): String? {
        return filename?.substringAfterLast('.', "")
    }

    private fun processPdf(file: MultipartFile, srcLang: String, tgtLang: String): List<PicDataDto> {
        val tempFile: Path = Files.createTempFile(Paths.get(System.getProperty("java.io.tmpdir")), "uploaded-", ".pdf")
        file.transferTo(tempFile.toFile())

        PDDocument.load(tempFile.toFile()).use { document ->
            val stripper = PDFTextStripper()
            val text = stripper.getText(document).trim()

            val result =
                text.ifEmpty {
                    // PDF содержит изображения, распознаем текст с помощью OCR
                    readImagesFromPdf(document)
                }
            val translatedText = translateText(result, srcLang, "en")
            val analyzedText = analyzerService.analyzeText(translatedText)
            val translateToTgtLang = translateText(analyzedText, "en", tgtLang)
            val resultWords = translateToTgtLang.split(",").toSet()
            return pictureFinder.findPictureByWords(resultWords)
        }
    }

    private fun processImage(file: MultipartFile, srcLang: String, tgtLang: String): List<PicDataDto> {
        val tempFile = Files.createTempFile(null, ".tmp").toFile()
        file.transferTo(tempFile)

        val image = ImageIO.read(tempFile)
        val processedImage = preprocessImage(image) // Добавляем предварительную обработку изображения// Увеличиваем масштаб изображения
        val text = extractTextFromImage(processedImage)
        val translatedText = translateText(text, srcLang, tgtLang)
        val analyzedText = analyzerService.analyzeText(translatedText)
        val translateToTgtLang = translateText(analyzedText, "en", tgtLang)
        val resultWords = translateToTgtLang.split(",").toSet()
        return pictureFinder.findPictureByWords(resultWords)
    }


    private fun readImagesFromPdf(document: PDDocument): String {
        val pdfRenderer = PDFRenderer(document)
        val sb = StringBuilder()

        for (page in 0 until document.numberOfPages) {
            val bim = pdfRenderer.renderImageWithDPI(page, 300F, ImageType.RGB)
            val processedImage = preprocessImage(bim) // Добавляем предварительную обработку изображения// Увеличиваем масштаб изображения
            sb.append(extractTextFromImage(processedImage)).append("\n") // Используйте "eng" или другой язык по умолчанию
        }

        return sb.toString()
    }

    private fun preprocessImage(image: BufferedImage): BufferedImage {
        val scaledImage = scaleImage(image) // Увеличиваем масштаб изображения
        val grayImage = convertToGrayscale(scaledImage) // Преобразование в оттенки серого
        return enhanceImage(grayImage) // Улучшаем изображение
    }

    private fun scaleImage(image: BufferedImage): BufferedImage {
        val width = (image.width * 1.5).toInt()
        val height = (image.height * 1.5).toInt()
        val scaledImage = BufferedImage(width, height, image.type)

        val g2d = scaledImage.createGraphics()
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.drawImage(image, 0, 0, width, height, null)
        g2d.dispose()

        return scaledImage
    }


    private fun convertToGrayscale(image: BufferedImage): BufferedImage {
        val grayImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_BYTE_GRAY)
        val g = grayImage.createGraphics()
        g.drawImage(image, 0, 0, null)
        g.dispose()
        return grayImage
    }

    private fun enhanceImage(image: BufferedImage): BufferedImage {
        // Применяем фильтр для повышения контрастности и резкости
        val rescaleOp = RescaleOp(1.2f, 15.0f, null)
        return rescaleOp.filter(image, null)
    }

    private fun extractTextFromImage(image: BufferedImage): String {
        tesseract.setLanguage("eng")
        tesseract.setVariable("user_defined_dpi", "300")
        tesseract.setVariable("tessedit_char_blacklist", "_,.;:[]{}\"'\\/()|^%$@!?~`=<>")
        tesseract.setVariable("preserve_interword_spaces", "1")

        return try {
            tesseract.doOCR(image)
        } catch (e: TesseractException) {
            e.printStackTrace()
            "Error processing image: ${e.message}"
        }
    }

    private fun translateText(text: String, srcLang: String, tgtLang: String): String {
        val translationRequest = LibretranslateRq(q = text, source = srcLang, target = tgtLang)
        val translationResponse = translateClient.translate(translationRequest)
        return translationResponse.translatedText
    }



//    override fun processAndTranslatePdf(pdfDocument: PdfDocument, srcLang: String, tgtLang: String): List<PageTranslation> {
//        val translations = mutableListOf<PageTranslation>()
//
//        val parser = PdfDocumentContentParser(pdfDocument)
//        val strategy: ITextExtractionStrategy = SimpleTextExtractionStrategy()
//
//        for (pageNumber in 1..pdfDocument.numberOfPages) {
//            val pageText = parser.processContent(pageNumber, strategy).resultantText
//            val translation = translateText(pageText, srcLang, tgtLang)
//            translations.add(PageTranslation(pageNumber, translation))
//        }
//
//        return translations
//    }
//
//    private fun translateText(text: String, srcLang: String, tgtLang: String): String {
//        val translationRequest = LibretranslateRq(q = text, source = srcLang, target = tgtLang)
//        val translationResponse = translateClient.translate(translationRequest)
//        return translationResponse.translatedText
//    }
}