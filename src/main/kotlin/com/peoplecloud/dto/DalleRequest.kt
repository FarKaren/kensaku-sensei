package com.peoplecloud.dto

data class DalleRequest(
    val prompt: String
)

fun GenerateImageRq.toDalleRequest(): DalleRequest {
    return DalleRequest(
        prompt = prompt
    )
}
