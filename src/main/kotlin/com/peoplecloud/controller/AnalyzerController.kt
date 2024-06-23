package com.peoplecloud.controller


import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.peoplecloud.dto.PdfDoc
import com.peoplecloud.dto.TranslateRq
import com.peoplecloud.dto.TranslateRs
import com.peoplecloud.service.TranslateService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.*


@RestController
@RequestMapping("/api/v1")
class AnalyzerController(
    private val translateService: TranslateService
) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(AnalyzerController::class.java)
    }

    @PostMapping("/analyzer")
    fun translatePdf(@ModelAttribute request: TranslateRq): String {
        val results = mutableListOf<String>()

        for (file in request.multipartFiles) {
            if (file.isEmpty) continue
            val text = translateService.translateAndProcessPdf(file, request.srcLang, request.tgtLang)
            results.add(text)
        }

        return results.joinToString("\n")

    }

}




//    @PostMapping("/analyze")
//    fun analyzeText(@RequestBody text: TranslateRq): ResponseEntity<String> {
//        val scriptWrapperPath = "C:\\Users\\Karen\\PythonTranslate\\run_translate.bat"
//        val scriptDirPath = "C:\\Users\\Karen\\PythonTranslate\\translate_nlp.py"
//        val outputFilePath = "C:\\Users\\Karen\\PythonTranslate\\translated_text.txt"
//
//        // Формирование команды для запуска Python скрипта
//        try {
//            val processBuilder = ProcessBuilder(scriptWrapperPath, scriptDirPath, text.text)
//            val process = processBuilder.start()
//
//            val executorService = Executors.newFixedThreadPool(2)
//
//            // Запуск потоков для чтения вывода и ошибок
//            val outputFuture = executorService.submit {
//                readStream(
//                    process.inputStream,
//                    "TRANSLATE PROCESS"
//                )
//            }
//            val errorFuture = executorService.submit {
//                readStream(
//                    process.errorStream,
//                    "ERROR"
//                )
//            }
//
//            // Ждём завершения процесса
//            process.waitFor()
//
//            // Чтение данных из потоков (ждем их завершения)
//            outputFuture.get()
//            errorFuture.get()
//
//            executorService.shutdown()
//
//            // Проверяем, что файл создан
//            if (Files.exists(Paths.get(outputFilePath))) {
//                try {
//                    val translatedText = Files.readString(Paths.get(outputFilePath))
//                    return ResponseEntity.ok(translatedText)
//                } catch (e: IOException) {
//                    throw RuntimeException("Ошибка при чтении файла перевода", e)
//                }
//            } else {
//                throw FileNotFoundException("Файл перевода не был найден: $outputFilePath")
//            }
//        } catch (e: Exception) {
//            throw RuntimeException("Ошибка при выполнении скрипта перевода", e)
//        }
//    }
//
//    private fun readStream(stream: InputStream, logPrefix: String): String {
//        val output = StringBuilder()
//        BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
//            var line: String? = reader.readLine()
//            while (line != null) {
//                output.append(line).append("\n")
//                println("$logPrefix: $line")
//                line = reader.readLine()
//            }
//        }
//        return output.toString()
//    }
//}

//    @PostMapping("/analyzer")
//    fun translatePdf(@ModelAttribute request: TranslateRq): ResponseEntity<TranslateRs> {
//        if(request.multipartFiles.size > 5)
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
//
//        for (file in request.multipartFiles) {
//            val inputStream: InputStream = file.inputStream
//            val reader = PdfReader(inputStream)
//            val pdfDocument = PdfDocument(reader)
//            val numberOfPages = pdfDocument.numberOfPages
//
//            if (numberOfPages > 5) {
//                pdfDocument.close()
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
//            }
//            pdfDocument.close()
//        }
//
//        val translatedPages = request.multipartFiles.map { file ->
//            val inputStream: InputStream = file.inputStream
//            val reader = PdfReader(inputStream)
//            val pdfDocument = PdfDocument(reader)
//            val translations = translateService.translateAndProcessPdf(pdfDocument, request.srcLang, request.tgtLang)
//            pdfDocument.close()
////            PdfDoc(translations)
//        }
//
//        return ResponseEntity.ok(TranslateRs(emptyList()))
//
//    }
//
//}


//    @PostMapping("/translate")
//    fun translate(@RequestBody request: TranslateRq): ResponseEntity<TranslateRs> {
//        // Укажем полный путь к скрипту Python
//        val scriptWrapperPath = "C:\\Users\\Karen\\PythonTranslate\\run_translate.bat"
//        val scriptDirPath = "C:\\Users\\Karen\\PythonTranslate\\translate.py"
//        val outputFilePath = "C:\\Users\\Karen\\PythonTranslate\\translated_text.txt"
//
//        try {
//            val processBuilder =
//                ProcessBuilder(scriptWrapperPath, scriptDirPath, request.text, request.srcLang, request.tgtLang)
//            val process = processBuilder.start()
//
//            val executorService = Executors.newFixedThreadPool(2)
//
//            // Запуск потоков для чтения вывода и ошибок
//            val outputFuture = executorService.submit {
//                readStream(
//                    process.inputStream,
//                    "TRANSLATE PROCESS"
//                )
//            }
//            val errorFuture = executorService.submit {
//                readStream(
//                    process.errorStream,
//                    "ERROR"
//                )
//            }
//
//            // Ждём завершения процесса
//            process.waitFor()
//
//            // Чтение данных из потоков (ждем их завершения)
//            outputFuture.get()
//            errorFuture.get()
//
//            executorService.shutdown()
//
//            // Проверяем, что файл создан
//            if (Files.exists(Paths.get(outputFilePath))) {
//                try {
//                    val translatedText = Files.readString(Paths.get(outputFilePath))
//                    return ResponseEntity.ok(TranslateRs(translatedText.trim { it <= ' ' }))
//                } catch (e: IOException) {
//                    throw RuntimeException("Ошибка при чтении файла перевода", e)
//                }
//            } else {
//                throw FileNotFoundException("Файл перевода не был найден: $outputFilePath")
//            }
//        } catch (e: Exception) {
//            throw RuntimeException("Ошибка при выполнении скрипта перевода", e)
//        }
//    }
//
//
//    private fun readStream(stream: InputStream, logPrefix: String): String {
//        val output = StringBuilder()
//        BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
//            var line: String? = reader.readLine()
//            while (line != null) {
//                output.append(line).append("\n")
//                println("$logPrefix: $line")
//                line = reader.readLine()
//            }
//        }
//        return output.toString()
//    }
