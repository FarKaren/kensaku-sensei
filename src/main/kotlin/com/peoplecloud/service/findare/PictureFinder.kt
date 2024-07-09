package com.peoplecloud.service.findare

import com.peoplecloud.dto.processor.PicDataDto

interface PictureFinder {
    fun findPictureByWords(words: List<PicDataDto>): List<PicDataDto>
}