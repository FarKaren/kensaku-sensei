package com.peoplecloud.models

import com.peoplecloud.dto.processor.PicDataDto
import jakarta.persistence.*

@Entity
@Table(name = "portuguese")
open class Portuguese {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "japanese", nullable = false)
    open var japanese: String? = null

    @Column(name = "portuguese", nullable = false)
    open var portuguese: String? = null

    @ElementCollection
    @CollectionTable(name = "portuguese_pictures", joinColumns = [JoinColumn(name = "portuguese_id")])
    @Column(name = "pictures")
    open var pictures: List<String>? = null
}

fun Portuguese.toPicDataDto(): PicDataDto {
    return PicDataDto(
        id = id!!,
        sourceWord = japanese!!,
        targetWord = portuguese!!,
        urls = pictures ?: emptyList()
    )
}