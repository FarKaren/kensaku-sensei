package com.peoplecloud.service.findare

import com.peoplecloud.config.AppConfigProperty
import com.peoplecloud.dto.PicDataDto
import com.peoplecloud.service.processor.FileProcessorServiceImpl
import com.peoplecloud.service.processor.FileProcessorServiceImpl.Companion
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PictureFinderImpl(
    private val appConfig: AppConfigProperty,
    private val webDriver: WebDriver
) : PictureFinder {

    companion object {
        val log: Logger = LoggerFactory.getLogger(PictureFinder::class.java)
    }

    override fun findPictureByWords(words: Set<String>): List<PicDataDto> {
        log.info("method findPictureByWords() invoked")
       return words.map { word ->
            val url = appConfig.pictureSearchUrl.replace("query", word)
            try {
                webDriver.get(url)

                // Поиск изображений
                val imageElements = webDriver.findElements(By.cssSelector("img.tile--img__img")).take(3)
                val images = imageElements.map { it.getAttribute("src") }
                PicDataDto(
                    word = word,
                    urls = images
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return emptyList()
            }
        }
    }
}