//package com.peoplecloud.service
//
//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import com.peoplecloud.dto.ProcessFileRq
//import org.springframework.kafka.core.KafkaTemplate
//import org.springframework.stereotype.Service
//
//@Service
//class KafkaProducerService(
//    private val kafkaTemplate: KafkaTemplate<String, String>
//) {
//
//    companion object {
//        private const val TRANSLATION_TOPIC = "translation_requests"
//    }
//
//    fun sendTranslationRequest(request: ProcessFileRq) {
//        val message = jacksonObjectMapper().writeValueAsString(request)
//        kafkaTemplate.send(TRANSLATION_TOPIC, message)
//    }
//
//}