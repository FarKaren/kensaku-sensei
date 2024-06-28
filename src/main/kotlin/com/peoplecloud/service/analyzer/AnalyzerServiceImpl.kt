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
        private const val SCRIPT_DIR_PATH = "python/analyze_text_nlp.py"
        private const val OUTPUT_FILE_PATH = "temp/analyzed_text.txt"
    }

    override fun analyzeText(text: String): String {
        log.info("method analyzeText() invoked")
        val projectPath = Paths.get("").toAbsolutePath().toString()
        try {
            val processBuilder =
                ProcessBuilder("$projectPath/myenv/bin/python3", SCRIPT_DIR_PATH, text, "en")
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
            if (Files.exists(Paths.get(OUTPUT_FILE_PATH))) {
                try {
                    val translatedText = Files.readString(Paths.get(OUTPUT_FILE_PATH))
                    return this.parseTxtFile(translatedText)
                } catch (e: IOException) {
                    throw RuntimeException("Ошибка при чтении файла перевода", e)
                }
            } else {
                throw FileNotFoundException("Файл перевода не был найден: $OUTPUT_FILE_PATH")
            }
        } catch (e: Exception) {
            throw RuntimeException("Ошибка при выполнении скрипта перевода", e)
        }
    }

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

    private fun parseTxtFile(text: String): String {
        log.info("method parseTxtFile() invoked")
        val preparedText = text.replace("\n", "")
        val rareWords = preparedText.substringAfter("Rare Words:").substringBefore("Phrases:")
            .lowercase().split(",").toList()
        val phrases = preparedText.substringAfter("Phrases:").lowercase()

        val resultSet = HashSet<String>()

        rareWords.forEach {
            if (!phrases.contains(it))
                resultSet.add(it)
        }
        phrases.split(",").forEach { resultSet.add(it) }
        val filteredText =  resultSet.stream().collect(Collectors.joining(","))

        val japanesePattern = "[\\p{IsHiragana}\\p{IsKatakana}\\p{IsHan}]+"
        return Regex(japanesePattern).findAll(filteredText)
            .map { it.value }
            .filter { it.length > 1 }
            .joinToString(separator = ",")

    }

    fun filterWords(words: Set<String>): Set<String> {
        //Delete non symbols words and words which <3 symbols
        return words.map { phrase ->
            phrase.split(" ").map { word ->
                word.replace("-", "")
                    .replace("""[^a-zA-Zа-яА-Я]""".toRegex(), "")
            }.filter { it.length >= 3 }
                .joinToString(" ")
        }.filter { it.isNotEmpty() }.toSet()
    }

    fun filterPhrase(phrases: Set<String>): Set<String> {
        return phrases.map { phrase ->
            phrase.replace("-", " ")
                .split(" ").map { word ->
                    word.trim().replace("""[^a-zA-Zа-яА-Я]""".toRegex(), "")
                }.filter { it.length >= 3 }
                .joinToString(" ")
        }.filter { it.isNotEmpty() && it.contains(" ") }.toSet()
    }
}