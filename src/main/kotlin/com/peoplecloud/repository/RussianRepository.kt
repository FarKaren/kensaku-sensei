package com.peoplecloud.repository

import com.peoplecloud.models.Russian
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RussianRepository: JpaRepository<Russian, Long> {
    fun findByJapaneseIn(words: Set<String>): List<Russian>
}