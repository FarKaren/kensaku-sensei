package com.peoplecloud.models

import jakarta.persistence.*

@Entity
@Table(name = "deepl_langs")
open class DeeplLang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    open var id: Long? = null

    @Column(name = "language", nullable = false)
    open var language: String? = null

    @Column(name = "code", nullable = false)
    open var code: String? = null

    @Column(name = "is_supported", nullable = false)
    open var isSupported: Boolean? = false
}