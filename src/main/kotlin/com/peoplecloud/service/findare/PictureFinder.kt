package com.peoplecloud.service.findare

import com.peoplecloud.dto.PicDataDto

interface PictureFinder {
    fun findPictureByWords(words: List<PicDataDto>): List<PicDataDto>
}