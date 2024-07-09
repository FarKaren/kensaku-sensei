package com.peoplecloud.service.translate

import com.peoplecloud.dto.processor.PicDataDto

interface TranslateService {
    data class TranslateRs(
        val picFromBb: List<PicDataDto>,
        val newPic: List<PicDataDto>,
        val newWords: String
    )
    fun translateAndGetPicData(input: String, tgtLang: String): TranslateRs
}