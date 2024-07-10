package com.peoplecloud.config

import io.github.cdimascio.dotenv.Dotenv
import org.springframework.stereotype.Component

@Component
object Env {
    private val dotenv: Dotenv = Dotenv.configure()
        .ignoreIfMissing()
        .load()

    fun get(key: String): String? = dotenv[key]
}
