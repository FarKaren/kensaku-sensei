package com.peoplecloud.service.language

import com.peoplecloud.dto.language.LanguageListRs

interface LanguagesService {
    fun languages(): LanguageListRs
}