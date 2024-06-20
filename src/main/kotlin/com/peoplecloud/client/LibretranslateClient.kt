package com.peoplecloud.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping

@FeignClient(name = "libretranslate-client", url = "http://127.0.0.1:5000")
interface LibretranslateClient {

    @PostMapping("/translate")
    fun translate(request: LibretranslateRq): LibretranslateRs
}