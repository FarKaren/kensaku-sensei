package com.peoplecloud.service

import com.peoplecloud.client.LibretranslateClient
import com.peoplecloud.client.LibretranslateRq
import net.sourceforge.tess4j.Tesseract
import net.sourceforge.tess4j.TesseractException
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer
import org.apache.pdfbox.text.PDFTextStripper
import org.opencv.core.*
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.awt.image.RescaleOp
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Executors
import javax.imageio.ImageIO
import org.opencv.imgproc.Imgproc

@Service
class TranslateServiceImpl(
    private val translateClient: LibretranslateClient
) : TranslateService {

    // Путь к каталогу tessdata
    private val tessDataPath = "/usr/share/tesseract-ocr/4.00/tessdata/"

    // Инициализация Tesseract OCR
    private val tesseract = Tesseract().apply {
        setDatapath("/usr/share/tesseract-ocr/4.00/tessdata") // Укажите путь к tessdata
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

    override fun translateAndProcessPdf(file: MultipartFile, srcLang: String, tgtLang: String): String {
        return when (getExtension(file.originalFilename)) {
            "pdf" -> processPdf(file, srcLang, tgtLang)
            "jpeg", "jpg", "png" -> processImage(file, srcLang, tgtLang)
            else -> "Unsupported file type: ${file.originalFilename}"
        }

    }

    private fun getExtension(filename: String?): String? {
        return filename?.substringAfterLast('.', "")
    }

    private fun processPdf(file: MultipartFile, srcLang: String, tgtLang: String): String {
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
            val translatedText = translateText(result, srcLang, tgtLang)
            analyzeText(translatedText, srcLang, tgtLang)
            return ""
        }
    }

    private fun readImagesFromPdf(document: PDDocument): String {
        val pdfRenderer = PDFRenderer(document)
        val sb = StringBuilder()

        for (page in 0 until document.numberOfPages) {
            val bim = pdfRenderer.renderImageWithDPI(page, 300F, ImageType.RGB)
            //val processedImage = preprocessImage(bim) // Добавляем предварительную обработку изображения// Увеличиваем масштаб изображения
            val processedImage = processImageWithOpenCV(bim) // Обработка изображения с помощью OpenCV
            sb.append(extractTextFromImage(processedImage, "eng")).append("\n") // Используйте "eng" или другой язык по умолчанию
        }

        return sb.toString()
    }

    private fun processImageWithOpenCV(image: BufferedImage): BufferedImage {
        val mat = bufferedImageToMat(image)
        val sobelX = Mat()
        val sobelY = Mat()
        val absSobelX = Mat()
        val absSobelY = Mat()

        Imgproc.Sobel(mat, sobelX, CvType.CV_16S, 1, 0)
        Imgproc.Sobel(mat, sobelY, CvType.CV_16S, 0, 1)
        Imgproc.convertScaleAbs(sobelX, absSobelX)
        Imgproc.convertScaleAbs(sobelY, absSobelY)

        val edges = Mat()
        Core.addWeighted(absSobelX, 0.5, absSobelY, 0.5, 0.0, edges)

        val lines = Mat()
        Imgproc.HoughLinesP(edges, lines, 1.0, Math.PI / 180, 50, 50.0, 10.0)

        var angle = 0.0
        for (i in 0 until lines.rows()) {
            val l = lines.get(i, 0)
            val x1 = l[0]
            val y1 = l[1]
            val x2 = l[2]
            val y2 = l[3]
            angle += Math.atan2((y2 - y1), (x2 - x1))
        }

        angle /= lines.rows()
        angle = Math.toDegrees(angle)

        val rotationMatrix = Imgproc.getRotationMatrix2D(Point((mat.width() / 2).toDouble(), (mat.height() / 2).toDouble()), angle, 1.0)
        val rotatedMat = Mat()
        Imgproc.warpAffine(mat, rotatedMat, rotationMatrix, mat.size(), Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT, Scalar(255.0, 255.0, 255.0))

        return matToBufferedImage(rotatedMat)
    }

    private fun bufferedImageToMat(image: BufferedImage): Mat {
        val mat = Mat(image.height, image.width, CvType.CV_8UC3)
        val data = ByteArray(image.width * image.height * 3)
        val intArray = image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
        for (i in intArray.indices) {
            val rgb = intArray[i]
            data[i * 3] = (rgb and 0xFF).toByte()
            data[i * 3 + 1] = (rgb shr 8 and 0xFF).toByte()
            data[i * 3 + 2] = (rgb shr 16 and 0xFF).toByte()
        }
        mat.put(0, 0, data)
        return mat
    }

    private fun matToBufferedImage(mat: Mat): BufferedImage {
        val data = ByteArray(mat.width() * mat.height() * mat.elemSize().toInt())
        mat.get(0, 0, data)
        val bufferedImage = BufferedImage(mat.width(), mat.height(), BufferedImage.TYPE_3BYTE_BGR)
        bufferedImage.raster.setDataElements(0, 0, mat.width(), mat.height(), data)
        return bufferedImage
    }

    private fun scaleImage(image: BufferedImage, scale: Double): BufferedImage {
        val width = (image.width * scale).toInt()
        val height = (image.height * scale).toInt()
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

    private fun preprocessImage(image: BufferedImage): BufferedImage {
        val flippedImage = flipImageHorizontally(image) // Переворачиваем изображение по горизонтал
        val scaledImage = scaleImage(flippedImage, 1.5) // Увеличиваем масштаб изображения
        val grayImage = convertToGrayscale(scaledImage) // Преобразование в оттенки серого
        return enhanceImage(grayImage) // Улучшаем изображение
    }
    private fun flipImageHorizontally(image: BufferedImage): BufferedImage {
        val tx = AffineTransform.getScaleInstance(-1.0, 1.0)
        tx.translate(-image.width.toDouble(), 0.0)
        val op = AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)
        return op.filter(image, null)
    }


    private fun enhanceImage(image: BufferedImage): BufferedImage {
        // Применяем фильтр для повышения контрастности и резкости
        val rescaleOp = RescaleOp(1.2f, 15.0f, null)
        return rescaleOp.filter(image, null)
    }

    private fun processImage(file: MultipartFile, srcLang: String, tgtLang: String): String {
        val tempFile = Files.createTempFile(null, ".tmp").toFile()
        file.transferTo(tempFile)

        val image = ImageIO.read(tempFile)
        val text =  extractTextFromImage(image, "eng")
        val translatedText = translateText(text, srcLang, tgtLang)
        analyzeText(translatedText, srcLang, tgtLang)
        return ""
    }

    private fun extractTextFromImage(image: BufferedImage, lang: String): String {
        tesseract.setLanguage(lang)
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

    private fun analyzeText(translations: String, srcLang: String, tgtLang: String): List<String> {
        //Укажем полный путь к скрипту Python
        //val scriptWrapperPath = "C:\\Users\\Karen\\PythonTranslate\\run_translate.bat"
        val scriptDirPath = "python/analyze_text_nlp.py"
        val outputFilePath = "temp/analyzed_text.txt"

        try {
            val processBuilder =
                ProcessBuilder("python3", scriptDirPath, translations, "en")
            val process = processBuilder.start()

            val executorService = Executors.newFixedThreadPool(2)

            // Запуск потоков для чтения вывода и ошибок
            val outputFuture = executorService.submit {
                readStream(
                    process.inputStream,
                    "TRANSLATE PROCESS"
                )
            }
            val errorFuture = executorService.submit {
                readStream(
                    process.errorStream,
                    "ERROR"
                )
            }

            // Ждём завершения процесса
            process.waitFor()

            // Чтение данных из потоков (ждем их завершения)
            outputFuture.get()
            errorFuture.get()

            executorService.shutdown()

            // Проверяем, что файл создан
            if (Files.exists(Paths.get(outputFilePath))) {
                try {
                    val translatedText = Files.readString(Paths.get(outputFilePath))
                    return this.parseTxtFile(translatedText)
                } catch (e: IOException) {
                    throw RuntimeException("Ошибка при чтении файла перевода", e)
                }
            } else {
                throw FileNotFoundException("Файл перевода не был найден: $outputFilePath")
            }
        } catch (e: Exception) {
            throw RuntimeException("Ошибка при выполнении скрипта перевода", e)
        }
    }

    private fun readStream(stream: InputStream, logPrefix: String): String {
        val output = StringBuilder()
        BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
            var line: String? = reader.readLine()
            while (line != null) {
                output.append(line).append("\n")
                println("$logPrefix: $line")
                line = reader.readLine()
            }
        }
        return output.toString()
    }

    private fun parseTxtFile(text: String): List<String> {
        val preparedText = text.replace("\n", "")
        val scientificWords = preparedText.substringAfter("Scientific Words").substringBefore("Rare Words:")
            .split(",").toList()
        val rareWords = preparedText.substringAfter("Rare Words:").substringBefore("Phrases:")
            .split(",").toList()
        val phrases = preparedText.substringAfter("Phrases:")

        val resultSet = HashSet<String>()

        (scientificWords + rareWords).forEach {
            if (!phrases.contains(it))
                resultSet.add(it)
        }
        phrases.split(",").toList().forEach { resultSet.add(it) }
        val test = ""
        return emptyList()
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