package com.peoplecloud.service.processor

import com.peoplecloud.dto.processor.PicDataDto
import com.peoplecloud.exceptions.UnsupportedFileType
import com.peoplecloud.exceptions.UnsupportedLanguageException
import com.peoplecloud.models.English
import com.peoplecloud.models.Portuguese
import com.peoplecloud.models.Russian
import com.peoplecloud.repository.EnglishRepository
import com.peoplecloud.repository.PortugueseRepository
import com.peoplecloud.repository.RussianRepository
import com.peoplecloud.service.analyzer.AnalyzerService
import com.peoplecloud.service.findare.PictureFinder
import com.peoplecloud.service.translate.TranslateService
import net.sourceforge.tess4j.Tesseract
import net.sourceforge.tess4j.TesseractException
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
import javax.imageio.ImageIO

@Service
class FileProcessorServiceImpl(
    private val analyzerService: AnalyzerService,
    private val pictureFinder: PictureFinder,
    private val translateService: TranslateService,
    private val englishRepository: EnglishRepository,
    private val russianRepository: RussianRepository,
    private val portugueseRepository: PortugueseRepository
) : FileProcessorService {


    // Путь к каталогу tessdata
    private val tessDataPath = "/usr/share/tesseract-ocr/4.00/tessdata/"
    private val tesseractLangCode = mapOf(
        "Japanese" to "jpn",
        "English" to "eng",
        "Portuguese" to "prt",
        "Russian" to "rus"
    )
    private val deeplLangCode = mapOf(
        "Japanese" to "ja",
        "English" to "en-US",
        "Portuguese" to "pt-BR",
        "Russian" to "ru",
        "Arabic" to "ar",
        "Bulgarian" to "bg",
        "Czech" to "cs",
        "Danish" to "da",
        "German" to "de",
        "Greek" to "el",
        "Spanish" to "es",
        "Estonian" to "et",
        "French" to "fr",
        "Hungarian" to "hu",
        "Indonesian" to "id",
        "Italian" to "it",
        "Korean" to "ko",
        "Lithuanian" to "lt",
        "Latvian" to "lv",
        "Norwegian" to "nb",
        "Dutch" to "nl",
        "Polish" to "pl",
        "Romanian" to "ro",
        "Slovak" to "sk",
        "Slovenian" to "sl",
        "Swedish" to "sv",
        "Turkish" to "tr",
        "Ukrainian" to "uk",
        "Chinese" to "ch"
    )

    // Инициализация Tesseract OCR
    private val tesseract = Tesseract().apply {
        setDatapath(tessDataPath) // Укажите путь к tessdata
        setVariable("tessedit_ocr_engine_mode", "3") // OCR Engine mode: 3 - Default, based on what is available
        setVariable("textord_heavy_nr", "1") // Enables automatic orientation correction
        setVariable("textord_straight_baselines", "1") // Assists in correct text detection
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger(FileProcessorServiceImpl::class.java)
        private const val SRC_LANG = "Japanese"
    }


    override fun processManualInput(input: String, tgtLang: String): List<PicDataDto> {
        log.info("method processManualInput() invoked")

        val data = translateService.translateAndGetPicData(input, tgtLang)

        val newPicData = pictureFinder.findPictureByWords(data.newPic)
        addNewWordsToDatabase(newPicData, data.newWords, tgtLang)
        return newPicData + data.picFromBb
    }

    override fun processFile(file: MultipartFile, tgtLang: String): List<PicDataDto> {
        log.info("method processFile() invoked")
        this.validateLanguage(tgtLang)
        val processedText =
            when (getExtension(file.originalFilename).lowercase()) {
                "txt" -> processTxt(file)
                "docx", "doc" -> processDoc(file)
                "pdf" -> processPdf(file)
                "jpeg", "jpg", "png" -> processImage(file)
                else -> throw UnsupportedFileType("Unsupported file type: ${file.originalFilename}")
            }
        return handleText(processedText, tgtLang)
    }

    private fun getExtension(filename: String?): String {
        log.info("method getExtension() invoked")
        if(filename.isNullOrEmpty())
            throw UnsupportedFileType("Unsupported file type: $filename")
        return filename.substringAfterLast('.', "")
    }

    private fun processTxt(file: MultipartFile): String {
        log.info("method processTxt() invoked")
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
        log.info("method processDoc() invoked")
        val inputStream = file.inputStream

        val document = XWPFDocument(inputStream)
        val stringBuilder = StringBuilder()

        for (paragraph in document.paragraphs) {
            stringBuilder.append(paragraph.text).append(System.lineSeparator())
        }

        document.close()
        return stringBuilder.toString()
    }

    private fun processPdf(file: MultipartFile): String {
        log.info("method processPdf() invoked")
        val tempFile: Path = Files.createTempFile(Paths.get(System.getProperty("java.io.tmpdir")), "uploaded-", ".pdf")
        file.transferTo(tempFile.toFile())

        PDDocument.load(tempFile.toFile()).use { document ->
            val stripper = PDFTextStripper()
            val text = stripper.getText(document).trim()

            return text.ifEmpty {
                // PDF содержит изображения, распознаем текст с помощью OCR
                readImagesFromPdf(document)
            }
        }
    }

    private fun processImage(file: MultipartFile): String {
        log.info("method processImage() invoked")
        val tempFile = Files.createTempFile(null, ".tmp").toFile()
        file.transferTo(tempFile)

        val image = ImageIO.read(tempFile)
        val processedImage =
            preprocessImage(image) // Добавляем предварительную обработку изображения// Увеличиваем масштаб изображения
        return extractTextFromImage(processedImage)
    }


    private fun readImagesFromPdf(document: PDDocument): String {
        log.info("method readImagesFromPdf() invoked")
        val pdfRenderer = PDFRenderer(document)
        val sb = StringBuilder()

        for (page in 0 until document.numberOfPages) {
            val bim = pdfRenderer.renderImageWithDPI(page, 300F, ImageType.RGB)
            val processedImage =
                preprocessImage(bim) // Добавляем предварительную обработку изображения// Увеличиваем масштаб изображения
            sb.append(extractTextFromImage(processedImage))
                .append("\n") // Используйте "eng" или другой язык по умолчанию
        }

        return sb.toString()
    }

    private fun preprocessImage(image: BufferedImage): BufferedImage {
        log.info("method preprocessImage() invoked")
        val scaledImage = scaleImage(image) // Увеличиваем масштаб изображения
        val grayImage = convertToGrayscale(scaledImage) // Преобразование в оттенки серого
        return enhanceImage(grayImage) // Улучшаем изображение
    }

    private fun scaleImage(image: BufferedImage): BufferedImage {
        log.info("method scaleImage() invoked")
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
        log.info("method convertToGrayscale() invoked")
        val grayImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_BYTE_GRAY)
        val g = grayImage.createGraphics()
        g.drawImage(image, 0, 0, null)
        g.dispose()
        return grayImage
    }

    private fun enhanceImage(image: BufferedImage): BufferedImage {
        log.info("method enhanceImage() invoked")
        // Применяем фильтр для повышения контрастности и резкости
        val rescaleOp = RescaleOp(1.2f, 15.0f, null)
        return rescaleOp.filter(image, null)
    }

    private fun extractTextFromImage(image: BufferedImage): String {
        log.info("method extractTextFromImage() invoked")
        val code = tesseractLangCode[SRC_LANG]!!
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

    private fun validateLanguage(tgtLang: String) {
        log.info("method validateLanguage() invoked")
        if (!tesseractLangCode.keys.contains(tgtLang) ||
            !deeplLangCode.keys.contains(tgtLang)
        )
            throw UnsupportedLanguageException(tgtLang)
    }

    private fun handleText(text: String, tgtLang: String): List<PicDataDto> {
        log.info("method handleText() invoked")
        val analyzedText = analyzerService.analyzeText(text)

        val data = translateService.translateAndGetPicData(analyzedText, tgtLang)

        val newPicData = pictureFinder.findPictureByWords(data.newPic)
        addNewWordsToDatabase(newPicData, data.newWords, tgtLang)
        return newPicData + data.picFromBb
    }

    private fun addNewWordsToDatabase(pictureData: List<PicDataDto>, newWords: String, tgtLang: String) {
        val newPicDataList = pictureData.filter { newWords.contains(it.sourceWord) }
        when (tgtLang) {
            "English" -> {
                val data = newPicDataList.map {
                    English().apply {
                        japanese = it.sourceWord
                        english = it.targetWord
                        pictures = it.urls
                    }
                }
                englishRepository.saveAll(data)
            }

            "Russian" -> {
                val data = newPicDataList.map {
                    Russian().apply {
                        japanese = it.sourceWord
                        russian = it.targetWord
                        pictures = it.urls
                    }
                }
                russianRepository.saveAll(data)
            }

            else -> {
                val data = newPicDataList.map {
                    Portuguese().apply {
                        japanese = it.sourceWord
                        portuguese = it.targetWord
                        pictures = it.urls
                    }
                }
                portugueseRepository.saveAll(data)
            }
        }
    }
}