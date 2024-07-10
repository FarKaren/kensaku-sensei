package com.peoplecloud.client.unsplash

import com.peoplecloud.config.Env
import feign.RequestInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UnsplashClientConfig {

    private val unsplashApiKey: String = Env.get("UNSPLASH_API_KEY")
        ?: throw IllegalArgumentException("UNSPLASH_API_KEY is not defined")

    @Bean
    fun unsplashRequestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            template.header("Authorization", "Client-ID $unsplashApiKey")
        }
    }
}