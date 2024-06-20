package com.peoplecloud.client

import com.peoplecloud.dto.TranslateRq

data class LibretranslateRq(
    val q: String,
    val source: String,
    val target: String,
    val format: String = "text"
)

