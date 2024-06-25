package com.peoplecloud.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties
data class AppConfigProperty(
    val pictureSearchUrl: String
)
