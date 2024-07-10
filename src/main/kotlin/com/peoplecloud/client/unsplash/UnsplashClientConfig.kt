package com.peoplecloud.client.unsplash

import feign.RequestInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UnsplashClientConfig {

    @Bean
    fun unsplashRequestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            template.header("Authorization", "Client-ID yBRSRK9V_6PjaOPSiWZgujpfVV6y82nfltue-EnHsCk")
        }
    }
}