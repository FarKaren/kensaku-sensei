package com.peoplecloud.repository

import com.peoplecloud.models.SynonymModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SynonymRepository: JpaRepository<SynonymModel, Long> {
}