package com.peoplecloud.repository

import com.peoplecloud.models.DeeplLang
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DeeplLangRepository: JpaRepository<DeeplLang, Long> {
    fun findByLanguage(language: String): DeeplLang?
}