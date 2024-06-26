package com.peoplecloud.service.processor

import com.peoplecloud.client.LibretranslateClient
import com.peoplecloud.client.LibretranslateRq
import com.peoplecloud.dto.PicDataDto
import com.peoplecloud.dto.exception.UnsupportedLanguageException
import com.peoplecloud.exceptions.UnsupportedFileType
import com.peoplecloud.service.analyzer.AnalyzerService
import com.peoplecloud.service.findare.PictureFinder
import net.sourceforge.tess4j.Tesseract
import net.sourceforge.tess4j.TesseractException
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.awt.image.RescaleOp
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.imageio.ImageIO

@Service
class FileProcessorServiceImpl(
    private val translateClient: LibretranslateClient,
    private val analyzerService: AnalyzerService,
    private val pictureFinder: PictureFinder
) : FileProcessorService {

    // Путь к каталогу tessdata
    private val tessDataPath = "/usr/share/tesseract-ocr/4.00/tessdata/"
    private val tesseractLangCode = mapOf(
        "Japanese" to "jpn",
        "English" to "eng",
        "Portuguese" to "prt",
        "Russian" to "rus"
    )
    private val libretranslateLangCode = mapOf(
        "Japanese" to "ja",
        "English" to "en",
        "Portuguese" to "pt",
        "Russian" to "ru"
    )

    // Инициализация Tesseract OCR
    private val tesseract = Tesseract().apply {
        setDatapath(tessDataPath) // Укажите путь к tessdata
        setVariable("tessedit_ocr_engine_mode", "3") // OCR Engine mode: 3 - Default, based on what is available
        setVariable("textord_heavy_nr", "1") // Enables automatic orientation correction
        setVariable("textord_straight_baselines", "1") // Assists in correct text detection
    }


    override fun processFile(file: MultipartFile, srcLang: String, tgtLang: String): List<PicDataDto> {
        this.validateLanguage(srcLang, tgtLang)
        val processedText =
            when (getExtension(file.originalFilename)) {
                "txt" -> processTxt(file)
                "docx", "doc" -> processDoc(file)
                "pdf" -> processPdf(file, srcLang)
                "jpeg", "jpg", "png" -> processImage(file, srcLang)
                else -> throw UnsupportedFileType("Unsupported file type: ${file.originalFilename}")
            }
        return handleText(processedText, srcLang, tgtLang)
    }

    private fun getExtension(filename: String?): String? {
        return filename?.substringAfterLast('.', "")
    }

    private fun processTxt(file: MultipartFile): String {
        val inputStream = file.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            stringBuilder.append(line).append(System.lineSeparator())
        }

        reader.close()
        return stringBuilder.toString()
    }

    private fun processDoc(file: MultipartFile): String {
        val inputStream = file.inputStream

        val document = XWPFDocument(inputStream)
        val stringBuilder = StringBuilder()

        for (paragraph in document.paragraphs) {
            stringBuilder.append(paragraph.text).append(System.lineSeparator())
        }

        document.close()
        return stringBuilder.toString()
    }

    private fun processPdf(file: MultipartFile, srcLang: String): String {
        val tempFile: Path = Files.createTempFile(Paths.get(System.getProperty("java.io.tmpdir")), "uploaded-", ".pdf")
        file.transferTo(tempFile.toFile())

        PDDocument.load(tempFile.toFile()).use { document ->
            val stripper = PDFTextStripper()
            val text = stripper.getText(document).trim()

            return text.ifEmpty {
                    // PDF содержит изображения, распознаем текст с помощью OCR
                    readImagesFromPdf(document, srcLang)
                }
//            val translatedText = translateText(result, srcLang, "English")
//            val analyzedText = analyzerService.analyzeText(translatedText)
//            val translateToTgtLang = translateText(analyzedText, "English", tgtLang)
//            val resultWords = translateToTgtLang.split(",").toSet()
//            return pictureFinder.findPictureByWords(resultWords)
        }
    }

    private fun processImage(file: MultipartFile, srcLang: String): String {
        val tempFile = Files.createTempFile(null, ".tmp").toFile()
        file.transferTo(tempFile)

        val image = ImageIO.read(tempFile)
        val processedImage =
            preprocessImage(image) // Добавляем предварительную обработку изображения// Увеличиваем масштаб изображения
        return extractTextFromImage(processedImage, srcLang)
//        val translatedText = translateText(text, srcLang, "English")
//        val analyzedText = analyzerService.analyzeText(translatedText)
//        val translateToTgtLang = translateText(analyzedText, "English", tgtLang)
//        val resultWords = translateToTgtLang.split(",").toSet()
//        return pictureFinder.findPictureByWords(resultWords)
    }


    private fun readImagesFromPdf(document: PDDocument, srcLang: String): String {
        val pdfRenderer = PDFRenderer(document)
        val sb = StringBuilder()

        for (page in 0 until document.numberOfPages) {
            val bim = pdfRenderer.renderImageWithDPI(page, 300F, ImageType.RGB)
            val processedImage =
                preprocessImage(bim) // Добавляем предварительную обработку изображения// Увеличиваем масштаб изображения
            sb.append(extractTextFromImage(processedImage, srcLang))
                .append("\n") // Используйте "eng" или другой язык по умолчанию
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

    private fun extractTextFromImage(image: BufferedImage, lang: String): String {
        val code = tesseractLangCode[lang]!!
        tesseract.setLanguage(code)
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
        val translationRequest = LibretranslateRq(
            q = text,
            source = libretranslateLangCode[srcLang]!!,
            target = libretranslateLangCode[tgtLang]!!
        )
        val translationResponse = translateClient.translate(translationRequest)
        return translationResponse.translatedText
    }

    private fun validateLanguage(srcLang: String, tgtLang: String) {
        if (!tesseractLangCode.keys.contains(srcLang) ||
            !libretranslateLangCode.keys.contains(srcLang)
        )
            throw UnsupportedLanguageException(srcLang)

        if (!tesseractLangCode.keys.contains(tgtLang) ||
            !libretranslateLangCode.keys.contains(tgtLang)
        )
            throw UnsupportedLanguageException(tgtLang)
    }

    private fun handleText(text: String, srcLang: String, tgtLang: String): List<PicDataDto> {
        val translatedText = translateText(text, srcLang, "English")
        val analyzedText = analyzerService.analyzeText(translatedText)
        val translateToTgtLang = translateText(analyzedText, "English", tgtLang)
        val resultWords = translateToTgtLang.split(",").toSet()
        return pictureFinder.findPictureByWords(resultWords)
    }
}