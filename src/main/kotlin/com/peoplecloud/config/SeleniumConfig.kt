package com.peoplecloud.config

import io.github.bonigarcia.wdm.WebDriverManager
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.logging.LoggingPreferences
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.logging.Level

@Configuration
class SeleniumConfig {

    @Bean
    fun webDriver(): WebDriver {
        WebDriverManager.chromedriver().setup()  // Автоматическое управление версией chromedriver

        val options = ChromeOptions().apply {
            //addArguments("--headless")  // Убедитесь, что вам действительно нужно использовать headless режим
            addArguments("--no-sandbox")
            addArguments("--disable-dev-shm-usage")
            addArguments("--disable-web-security")
            addArguments("--disable-gpu")
            addArguments("--remote-allow-origins=*")  // Добавьте эту строку
        }

        val logPrefs = LoggingPreferences().apply {
            enable(LogType.PERFORMANCE, Level.ALL)
            enable(LogType.BROWSER, Level.ALL)
        }

        options.setCapability("goog:loggingPrefs", logPrefs)

        return ChromeDriver(options)
    }
}