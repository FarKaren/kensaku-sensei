package com.peoplecloud.client

data class LibretranslateRq(
    val q: String,
    val source: String,
    val target: String,
    val format: String = "text"
)

