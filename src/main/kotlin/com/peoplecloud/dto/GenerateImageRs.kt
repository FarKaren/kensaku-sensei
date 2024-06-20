package com.peoplecloud.dto

import java.net.URL

data class GenerateImageRs(
    val data: List<Data>
)

fun DalleResponse.toGenerateImageRs(): GenerateImageRs {
    return GenerateImageRs(
        data = data
    )
}
