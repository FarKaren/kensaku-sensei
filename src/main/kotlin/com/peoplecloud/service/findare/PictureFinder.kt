package com.peoplecloud.service.findare

import com.peoplecloud.dto.PicDataDto

interface PictureFinder {
    fun findPictureByWords(words: Set<String>): List<PicDataDto>
}