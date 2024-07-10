package com.peoplecloud.dto.word

data class YandexResponse(
    val def: List<Definition> = emptyList()
)

data class Definition(
    val text: String,
    val pos: String?,
    val tr: List<Translation> = emptyList()
)

data class Translation(
    val text: String,
    val pos: String?,
    val syn: List<Synonym> = emptyList(),
)

data class Synonym(
    val text: String
)

