package com.peoplecloud.service.analyzer

import com.peoplecloud.service.processor.FileProcessorServiceImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Executors
import java.util.stream.Collectors

@Service
class AnalyzerServiceImpl : AnalyzerService {

    companion object {
        val log: Logger = LoggerFactory.getLogger(FileProcessorServiceImpl::class.java)
        private const val SCRIPT_DIR_PATH = "kensakusensei/python/analyze_text_nlp.py"
        private const val OUTPUT_FILE_PATH = "kensakusensei/files/analyzed_text.txt"
    }

    override fun analyzeText(text: String): String {
        log.info("method analyzeText() invoked")
        val userHomePath = System.getProperty("user.home")
        val projectPath = Paths.get("").toAbsolutePath().toString()
        log.info("Project path: $projectPath")
        log.info("Path to python: $userHomePath/$SCRIPT_DIR_PATH")

        try {
            log.info("Building process with script at: $userHomePath/$SCRIPT_DIR_PATH")
            val processBuilder = ProcessBuilder(
                "$userHomePath/myenv/bin/python3",
                "$userHomePath/$SCRIPT_DIR_PATH",
                text
            )
            processBuilder.directory(File(projectPath))
            log.info("Starting process...")
            val process = processBuilder.start()

            val executorService = Executors.newFixedThreadPool(2)

            // Запуск потоков для чтения вывода и ошибок
            val outputFuture = executorService.submit {
                logStream(process.inputStream, "TRANSLATE PROCESS")
            }
            val errorFuture = executorService.submit {
                logStream(process.errorStream, "ERROR")
            }

            // Ждём завершения процесса
            val exitCode = process.waitFor()
            log.info("Process exited with code: $exitCode")

            // Чтение данных из потоков (ждем их завершения)
            outputFuture.get()
            errorFuture.get()

            executorService.shutdown()

            // Проверяем, что файл создан
            val outputPath = Paths.get("$userHomePath/$OUTPUT_FILE_PATH")
            if (Files.exists(outputPath)) {
                try {
                    val result = Files.readString(outputPath)
                    log.info("Translation result: $result")
                    return filterWords(result)
                } catch (e: IOException) {
                    log.error("Error reading translation file", e)
                    throw RuntimeException("Ошибка при чтении файла перевода", e)
                }
            } else {
                log.error("Translation file not found: $userHomePath/$OUTPUT_FILE_PATH")
                throw FileNotFoundException("Файл перевода не был найден: $userHomePath/$OUTPUT_FILE_PATH")
            }
        } catch (e: Exception) {
            log.error("Error running translation script", e)
            throw RuntimeException("Ошибка при выполнении скрипта перевода", e)
        }
    }

    private fun logStream(stream: InputStream, streamName: String) {
        try {
            BufferedReader(InputStreamReader(stream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    log.debug("[$streamName] $line")
                }
            }
        } catch (e: IOException) {
            log.error("Error reading stream $streamName", e)
        }
    }

//    override fun analyzeText(text: String): String {
//        log.info("method analyzeText() invoked")
//        val projectPath = Paths.get("").toAbsolutePath().toString()
//        try {
//            val processBuilder =
//                ProcessBuilder("$projectPath/myenv/bin/python3", SCRIPT_DIR_PATH, text)
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
//            if (Files.exists(Paths.get(OUTPUT_FILE_PATH))) {
//                try {
//                    val result = Files.readString(Paths.get(OUTPUT_FILE_PATH))
//                    return filterWords(result)
//                } catch (e: IOException) {
//                    throw RuntimeException("Ошибка при чтении файла перевода", e)
//                }
//            } else {
//                throw FileNotFoundException("Файл перевода не был найден: $OUTPUT_FILE_PATH")
//            }
//        } catch (e: Exception) {
//            throw RuntimeException("Ошибка при выполнении скрипта перевода", e)
//        }
//    }

    private fun readStream(stream: InputStream, logPrefix: String): String {
        log.info("method readStream() invoked")
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

    fun filterWords(input: String): String {
        val set = input.substringAfter("Rare Words:")
            .trim()
            .split(";")
            .toSet()

        return set.stream().collect(Collectors.joining(";"))
    }
}