package com.peoplecloud.controller

import com.peoplecloud.dto.word.WordCardRs
import com.peoplecloud.models.Languages
import com.peoplecloud.service.WordService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/word")
class WordController(
    private val wordService: WordService
) {

    @GetMapping("/card")
    fun getWordCard(
        @RequestParam wordId: Long,
        @RequestParam language: Languages
    ): ResponseEntity<WordCardRs> {
        val result = wordService.getWordCard(wordId, language)
        return ResponseEntity.ok(WordCardRs(result))
    }
}