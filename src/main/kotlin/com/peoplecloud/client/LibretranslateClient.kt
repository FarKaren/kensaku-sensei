package com.peoplecloud.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping

@FeignClient(name = "libretranslate-client", url = "\${feign.client.libretranslate-client.url}")
interface LibretranslateClient {

    @PostMapping("/translate")
    fun translate(request: LibretranslateRq): LibretranslateRs
}