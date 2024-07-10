package com.peoplecloud.client.unsplash

import com.peoplecloud.config.ApiKeysProperty
import feign.RequestInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UnsplashClientConfig(
    private val apiKeysProperty: ApiKeysProperty
) {

    @Bean
    fun unsplashRequestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            template.header("Authorization", "Client-ID ${apiKeysProperty.unsplashApiKey}")
        }
    }
}