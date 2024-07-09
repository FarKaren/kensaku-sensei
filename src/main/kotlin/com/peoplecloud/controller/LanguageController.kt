package com.peoplecloud.controller

import com.peoplecloud.dto.language.LanguageListRs
import com.peoplecloud.service.language.LanguagesService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/language")
class LanguageController(
    private val languagesService: LanguagesService
) {

    @GetMapping
    fun languages(): ResponseEntity<LanguageListRs> {
        val result = languagesService.languages()
        return ResponseEntity.ok(result)
    }
}