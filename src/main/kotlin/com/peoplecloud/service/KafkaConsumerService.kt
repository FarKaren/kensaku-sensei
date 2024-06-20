//package com.peoplecloud.service
//
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import com.peoplecloud.dto.TranslateRq
//import kotlinx.coroutines.runBlocking
//import org.springframework.kafka.annotation.KafkaListener
//import org.springframework.kafka.core.KafkaTemplate
//import org.springframework.kafka.support.Acknowledgment
//import org.springframework.stereotype.Service
//
//@Service
//class KafkaConsumerService(
//    private val kafkaTemplate: KafkaTemplate<String, String>
//) {
//
//    @KafkaListener(topics = ["translation_requests"], groupId = "translation_group")
//    fun consumeMessage(message: String, acknowledgment: Acknowledgment) {
//        val request: TranslateRq  = jacksonObjectMapper().readValue(message)
//        runBlocking {
//            val result = processTranslationRequest(request)
//            sendTranslationResult(result)
//            acknowledgment.acknowledge()
//        }
//    }
//
//    private suspend fun processTranslationRequest(request: TranslationRequest): TranslationResult {
//        val scriptPath = "path/to/your/script.py"
//        val processBuilder = ProcessBuilder("python3", scriptPath, request.text, request.srcLang, request.tgtLang)
//        processBuilder.redirectErrorStream(true)
//        val process = processBuilder.start()
//
//        val result = withContext(Dispatchers.IO) {
//            process.inputStream.bufferedReader().readText()
//        }
//        process.waitFor()
//
//        return TranslationResult(request.text, result, request.srcLang, request.tgtLang)
//    }
//
//    private fun sendTranslationResult(result: TranslationResult) {
//        val topic = "translation_results"
//        val message = objectMapper.writeValueAsString(result)
//        kafkaTemplate.send(topic, message)
//    }
//}
//}