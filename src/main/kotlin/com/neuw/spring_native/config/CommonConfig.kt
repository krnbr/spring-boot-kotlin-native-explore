package com.neuw.spring_native.config

import com.neuw.spring_native.config.handlers.CustomResponseBodyResultHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.reactive.accept.RequestedContentTypeResolver

@Configuration
class CommonConfig {

    @Autowired
    private lateinit var serverCodecConfigurer: ServerCodecConfigurer

    @Autowired
    private lateinit var requestedContentTypeResolver: RequestedContentTypeResolver

    @Bean
    fun customResponseBodyResultHandler(): CustomResponseBodyResultHandler {
        return CustomResponseBodyResultHandler(
            serverCodecConfigurer
                .writers, requestedContentTypeResolver
        )
    }

}