package com.peoplecloud.config

import com.deepl.api.Translator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DeeplConfig {

    @Bean
    fun translator(): Translator {
        return Translator("5423e90d-b9a8-4539-8f2f-ad0ba068e79e:fx")
    }
}