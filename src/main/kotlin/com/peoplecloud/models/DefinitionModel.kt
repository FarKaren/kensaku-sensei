package com.peoplecloud.models

import jakarta.persistence.*

@Entity
@Table(name = "definitions")
open class DefinitionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open val id: Long? = null

    @Column(name = "text")
    open var text: String? = null

    @Column(name = "pos")
    open var pos: String? = null

    @Column(name = "word_id", nullable = false)
    open var wordId: Long? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false)
    open var language: Languages = Languages.Portuguese

    @OneToMany(mappedBy = "definition", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    open var translations: MutableList<TranslationModel> = ArrayList()
}