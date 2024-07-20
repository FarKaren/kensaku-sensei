package com.peoplecloud.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "api-keys")
data class ApiKeysProperty(
    val yandexDictionaryApiKey: String,
    val unsplashApiKey: String,
    val deeplApiKey: String
)