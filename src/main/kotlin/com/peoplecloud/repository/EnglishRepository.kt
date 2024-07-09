package com.peoplecloud.repository

import com.peoplecloud.models.English
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EnglishRepository: JpaRepository<English, Long> {
    fun findByJapaneseIn(words: Set<String>): List<English>
}