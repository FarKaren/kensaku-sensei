package com.peoplecloud.service.language

import com.peoplecloud.dto.language.LanguageListRs
import com.peoplecloud.dto.language.toLanguageDto
import com.peoplecloud.repository.DeeplLangRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LanguagesServiceImpl(
    private val deeplLangRepository: DeeplLangRepository
) : LanguagesService {

    companion object {
        val log: Logger = LoggerFactory.getLogger(LanguagesServiceImpl::class.java)
    }

    @Transactional(readOnly = true)
    override fun languages(): LanguageListRs {
        log.info("languages invoked")

        val languages = deeplLangRepository.findAll()
            .filter { it.language != "Japanese" }
            .map { it.toLanguageDto() }
        return LanguageListRs(languages)
    }
}