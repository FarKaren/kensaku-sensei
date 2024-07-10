//package com.peoplecloud.config
//
//import org.openqa.selenium.WebDriver
//import org.openqa.selenium.chrome.ChromeDriver
//import org.openqa.selenium.chrome.ChromeOptions
//import org.openqa.selenium.logging.LogType
//import org.openqa.selenium.logging.LoggingPreferences
//import org.openqa.selenium.remote.RemoteWebDriver
//import org.slf4j.LoggerFactory
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import java.net.URL
//import java.util.logging.Level
//
//@Configuration
//class SeleniumConfig {
//
//    private val logger = LoggerFactory.getLogger(SeleniumConfig::class.java)
//
//    @Configuration
//    class SeleniumConfig {
//        @Bean
//        fun webDriver(): WebDriver {
//            val options = ChromeOptions().apply {
//                addArguments("--no-sandbox")
//                addArguments("--disable-dev-shm-usage")
//                addArguments("--disable-web-security")
//                addArguments("--disable-gpu")
//                addArguments("--headless")
//                addArguments("--disable-software-rasterizer")
//            }
//            return RemoteWebDriver(URL("http://localhost:4444/wd/hub"), options)
//        }
//    }
//
//
//
////    @Bean
////    fun webDriver(): WebDriver {
////        return try {
////            // Установка пути для chromedriver
////            System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver")
////
////            val options = ChromeOptions().apply {
////                addArguments("--no-sandbox")
////                addArguments("--disable-dev-shm-usage")
////                addArguments("--disable-web-security")
////                addArguments("--disable-gpu")
////                addArguments("--remote-allow-origins=*")
////                addArguments("--headless")
////                addArguments("--no-sandbox")
////                addArguments("--disable-setuid-sandbox")
////                addArguments("--disable-software-rasterizer")
////                setBinary("/usr/local/bin/chrome")
////            }
////
////            val logPrefs = LoggingPreferences().apply {
////                enable(LogType.PERFORMANCE, Level.ALL)
////                enable(LogType.BROWSER, Level.ALL)
////            }
////
////            options.setCapability("goog:loggingPrefs", logPrefs)
////
////            logger.info("Starting ChromeDriver with options: ${options.browserName}")
////            ChromeDriver(options)
////        } catch (e: Exception) {
////            logger.error("Failed to start ChromeDriver", e)
////            throw e
////        }
////    }
//}
