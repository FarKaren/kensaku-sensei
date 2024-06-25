package com.peoplecloud.service.findare

import com.peoplecloud.config.AppConfigProperty
import com.peoplecloud.dto.PicDataDto
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.springframework.stereotype.Service

@Service
class PictureFinderImpl(
    private val appConfig: AppConfigProperty,
    private val webDriver: WebDriver
) : PictureFinder {

    override fun findPictureByWords(words: Set<String>): List<PicDataDto> {
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