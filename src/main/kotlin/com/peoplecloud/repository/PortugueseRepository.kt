package com.peoplecloud.repository

import com.peoplecloud.models.Portuguese
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PortugueseRepository: JpaRepository<Portuguese, Long> {
    fun findByJapaneseIn(words: Set<String>): List<Portuguese>
}