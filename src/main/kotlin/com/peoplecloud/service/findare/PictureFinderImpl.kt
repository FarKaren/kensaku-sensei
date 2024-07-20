package com.peoplecloud.service.findare

import com.peoplecloud.client.unsplash.UnsplashClient
import com.peoplecloud.dto.processor.PicDataDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PictureFinderImpl(
    private val unsplashClient: UnsplashClient
) : PictureFinder {

    companion object {
        val log: Logger = LoggerFactory.getLogger(PictureFinder::class.java)
    }

    override fun findPictureByWords(words: List<PicDataDto>): List<PicDataDto> {
        log.info("method findPictureByWords() invoked")
        return words.filter { picData ->
            !(picData.targetWord.isEmpty() && picData.sourceWord.isEmpty())
        }
            .map {picData ->
                val word = picData.targetWord.ifEmpty { picData.sourceWord }
                try {
                    val searchResult = unsplashClient.searchPhotos(word, 3)
                    log.info("Photo description: ${searchResult.results.map { it.description }}")
                    picData.urls = searchResult.results.map { it.urls.regular }.toList()
                    picData
                } catch (e: Exception) {
                    e.printStackTrace()
                    return emptyList()
                }
            }
    }
}