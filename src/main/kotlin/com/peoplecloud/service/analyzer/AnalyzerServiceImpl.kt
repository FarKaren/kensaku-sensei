package com.peoplecloud.service.analyzer

import org.springframework.stereotype.Service
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Executors

@Service
class AnalyzerServiceImpl: AnalyzerService {

    companion object {
        private const val SCRIPT_DIR_PATH = "python/analyze_text_nlp.py"
        private const val OUTPUT_FILE_PATH = "temp/analyzed_text.txt"
    }

    override fun analyzeText(text: String): String {
        try {
            val processBuilder =
                ProcessBuilder("python3", SCRIPT_DIR_PATH, text, "en")
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
        return filterWords(resultSet).joinToString(",")
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
}