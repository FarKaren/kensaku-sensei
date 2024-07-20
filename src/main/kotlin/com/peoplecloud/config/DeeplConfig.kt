package com.peoplecloud.config

import com.deepl.api.Translator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DeeplConfig(
    private val apiKeysProperty: ApiKeysProperty
) {

    @Bean
    fun translator(): Translator {
        return Translator(apiKeysProperty.deeplApiKey)
    }
}