package com.peoplecloud.service.findare

import com.peoplecloud.config.AppConfigProperty
import com.peoplecloud.dto.PicDataDto
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class PictureFinderImpl(
    private val appConfig: AppConfigProperty,
    private val webDriver: WebDriver
) : PictureFinder {

    companion object {
        val log: Logger = LoggerFactory.getLogger(PictureFinder::class.java)
    }

    override fun findPictureByWords(words: List<PicDataDto>): List<PicDataDto> {
        log.info("method findPictureByWords() invoked")
        return words.filter { picData ->
            !(picData.targetWord.isEmpty() && picData.sourceWord.isEmpty())
        }
            .map { picData ->
                val word = picData.targetWord.ifEmpty { picData.sourceWord }
                val url = appConfig.pictureSearchUrl.replace("query", word)
                try {
                    webDriver.get(url)

                    // Создайте экземпляр WebDriverWait с тайм-аутом, например, 10 секунд
                    val wait = WebDriverWait(webDriver, Duration.ofSeconds(10))

                    // Ожидание, пока элементы изображения не будут видны
                    wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("img.tile--img__img")))

                    // Поиск изображений
                    val imageElements = webDriver.findElements(By.cssSelector("img.tile--img__img")).take(3)
                    val images = imageElements.map { it.getAttribute("src") }
                    picData.urls = images
                    picData
                } catch (e: Exception) {
                    e.printStackTrace()
                    return emptyList()
                }
            }
    }
}