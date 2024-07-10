package com.peoplecloud.models

import jakarta.persistence.*

@Entity
@Table(name = "synonyms")
open class SynonymModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open val id: Long? = null

    @Column(name = "text", nullable = false)
    open var text: String? = null

    @ManyToOne
    @JoinColumn(name = "translation_id", nullable = false)
    open var translation: TranslationModel? = null
}