package com.peoplecloud.repository

import com.peoplecloud.models.TranslationModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TranslationRepository: JpaRepository<TranslationModel, Long> {
}