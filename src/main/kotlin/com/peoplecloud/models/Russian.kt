package com.peoplecloud.models

import com.peoplecloud.dto.processor.PicDataDto
import jakarta.persistence.*

@Entity
@Table(name = "russian")
open class Russian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "japanese", nullable = false)
    open var japanese: String? = null

    @Column(name = "russian", nullable = false)
    open var russian: String? = null

    @ElementCollection
    @CollectionTable(name = "russian_pictures", joinColumns = [JoinColumn(name = "russian_id")])
    @Column(name = "pictures")
    open var pictures: List<String>? = null
}

fun Russian.toPicDataDto(): PicDataDto {
    return PicDataDto(
        sourceWord = japanese!!,
        targetWord = russian!!,
        urls = pictures ?: emptyList()
    )
}