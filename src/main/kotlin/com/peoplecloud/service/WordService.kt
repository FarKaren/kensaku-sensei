package com.peoplecloud.service

import com.peoplecloud.dto.word.WordCardRs
import com.peoplecloud.dto.word.YandexResponse
import com.peoplecloud.models.Languages

interface WordService {
    fun getWordCard(wordId: Long, language: Languages): YandexResponse
}