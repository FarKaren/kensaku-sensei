package com.peoplecloud.client.dictionary

import com.peoplecloud.dto.word.YandexResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "dictionaryClient", url = "https://dictionary.yandex.net/api/v1/dicservice.json")
interface YandexDictionaryClient {

    @GetMapping("/lookup")
    fun lookup(
        @RequestParam("key") apiKey: String,
        @RequestParam("lang") lang: String,
        @RequestParam("text") text: String
    ): YandexResponse
}