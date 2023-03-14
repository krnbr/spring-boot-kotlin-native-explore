package com.neuw.spring_native.web.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.neuw.spring_native.annotations.ApiResponse
import com.neuw.spring_native.web.models.PingResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * intention is to check if the responses
 * can be encapsulated within [ServiceResponse], this is managed if class is marked with [ApiResponse]
 */
@ApiResponse
@RestController
class PingController {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @GetMapping("v1/ping")
    fun getTest(@RequestParam(defaultValue = "Hello World") message: String,
                exchange: ServerWebExchange
    ): Mono<ObjectNode> {
        return Mono.just(objectMapper.createObjectNode().put("message", message).put("api", "ping-v1"))
    }

    @GetMapping("v2/ping")
    fun getTestV2(@RequestParam(defaultValue = "Hello World") message: String,
                exchange: ServerWebExchange
    ): ObjectNode {
        return objectMapper.createObjectNode().put("message", message).put("api", "ping-v2")
    }

    @GetMapping("v3/ping")
    fun getTestV3(@RequestParam(defaultValue = "Hello World") message: String,
                exchange: ServerWebExchange
    ): Mono<PingResponse> {
        return Mono.just(PingResponse("ping", "v1"))
    }

    @GetMapping("v4/ping")
    fun getTestV4(@RequestParam(defaultValue = "Hello World") message: String,
                  exchange: ServerWebExchange
    ): PingResponse {
        return PingResponse("ping", "v2")
    }

}