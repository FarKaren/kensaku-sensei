package com.peoplecloud.models

import com.peoplecloud.dto.processor.PicDataDto
import jakarta.persistence.*

@Entity
@Table(name = "english")
open class English {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "japanese", nullable = false)
    open var japanese: String? = null

    @Column(name = "english", nullable = false)
    open var english: String? = null

    @ElementCollection
    @CollectionTable(name = "english_pictures", joinColumns = [JoinColumn(name = "english_id")])
    @Column(name = "pictures")
    open var pictures: List<String>? = null
}

fun English.toPicDataDto(): PicDataDto {
    return PicDataDto(
        sourceWord = japanese!!,
        targetWord = english!!,
        urls = pictures ?: emptyList()
    )
}