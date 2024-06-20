//package com.peoplecloud.config
//
//import org.apache.kafka.clients.consumer.ConsumerConfig
//import org.apache.kafka.clients.producer.ProducerConfig
//import org.apache.kafka.common.serialization.StringDeserializer
//import org.apache.kafka.common.serialization.StringSerializer
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.kafka.annotation.EnableKafka
//import org.springframework.kafka.core.*
//import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
//import org.springframework.kafka.listener.ContainerProperties
//
//@EnableKafka
//@Configuration
//class KafkaConfig {
//
//    @Bean
//    fun kafkaTemplate(producerFactory: ProducerFactory<String, String>): KafkaTemplate<String, String> {
//        return KafkaTemplate(producerFactory)
//    }
//
//    @Bean
//    fun producerFactory(): ProducerFactory<String, String> {
//        val configProps = mapOf(
//            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
//            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
//            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
//        )
//        return DefaultKafkaProducerFactory(configProps)
//    }
//
//    @Bean
//    fun consumerFactory(): ConsumerFactory<String, String> {
//        val configProps = mapOf(
//            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
//            ConsumerConfig.GROUP_ID_CONFIG to "translation_group",
//            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
//            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
//        )
//        return DefaultKafkaConsumerFactory(configProps)
//    }
//
//    @Bean
//    fun kafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, String>): ConcurrentMessageListenerContainer<String, String> {
//        val containerProps = ContainerProperties("translation_requests")
//        containerProps.ackMode = ContainerProperties.AckMode.MANUAL
//        return ConcurrentMessageListenerContainer(consumerFactory, containerProps)
//    }
//}