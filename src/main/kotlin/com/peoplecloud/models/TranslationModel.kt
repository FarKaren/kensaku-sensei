package com.peoplecloud.models

import jakarta.persistence.*

@Entity
@Table(name = "translations")
open class TranslationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open val id: Long? = null

    @Column(name = "text", nullable = false)
    open var text: String? = null

    @Column(name = "pos")
    open var pos: String? = null


    @ManyToOne
    @JoinColumn(name = "definition_id", nullable = false)
    open var definition: DefinitionModel? = null


    @OneToMany(mappedBy = "translation", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    open var synonyms: MutableList<SynonymModel> = ArrayList()
}