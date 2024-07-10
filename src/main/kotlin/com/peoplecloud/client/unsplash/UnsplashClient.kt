package com.peoplecloud.client.unsplash

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "unsplashClient", url = "https://api.unsplash.com",
    configuration = [UnsplashClientConfig::class])
interface UnsplashClient {

    @GetMapping("/search/photos")
    fun searchPhotos(
        @RequestParam("query") query: String,
        @RequestParam("per_page") perPage: Int = 3)
    : SearchResult
}