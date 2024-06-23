package com.peoplecloud

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients


@SpringBootApplication
@EnableFeignClients
class TranslateHelperApplication

fun main(args: Array<String>) {
	runApplication<TranslateHelperApplication>(*args)
	nu.pattern.OpenCV.loadLocally()
}
