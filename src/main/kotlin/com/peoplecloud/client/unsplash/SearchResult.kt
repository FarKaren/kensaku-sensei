package com.peoplecloud.client.unsplash

import com.fasterxml.jackson.annotation.JsonProperty

data class SearchResult(
    val total: Int,
    @JsonProperty("total_pages")
    val totalPages: Int,
    val results: List<Photo>
)
data class Photo(
    val id: String,
    val description: String?,
    @JsonProperty("alt_description")
    val altDescription: String?,
    val urls: Urls
)

data class Urls(
    val raw: String
)

