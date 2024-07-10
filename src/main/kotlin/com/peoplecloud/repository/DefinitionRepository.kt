package com.peoplecloud.repository

import com.peoplecloud.models.DefinitionModel
import com.peoplecloud.models.Languages
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DefinitionRepository: JpaRepository<DefinitionModel, Long> {
    fun findByWordIdAndLanguage(id: Long, language: Languages): List<DefinitionModel>
}