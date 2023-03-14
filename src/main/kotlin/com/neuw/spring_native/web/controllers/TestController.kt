package com.neuw.spring_native.web.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.neuw.spring_native.annotations.ApiResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@RestController
class TestController {

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    /**
     * intention is to check if the response
     * wrapped in Mono can be encapsulated within [ServiceResponse], this is managed if method is marked with [ApiResponse]
     */
    @GetMapping("v1/test")
    @ApiResponse
    fun getTest(@RequestParam(defaultValue = "Hello World") message: String,
                exchange: ServerWebExchange
    ): Mono<ObjectNode> {
        return Mono.just(objectMapper.createObjectNode().put("message", message).put("api", "test-v1"))
    }

    /**
     * intention is to check if the response
     * not wrapped in Mono also can be encapsulated within [ServiceResponse], this is managed if method is marked with [ApiResponse]
     */
    @GetMapping("v2/test")
    @ApiResponse
    fun getTestV2(@RequestParam(defaultValue = "Hello World") message: String,
                exchange: ServerWebExchange
    ): ObjectNode {
        return objectMapper.createObjectNode().put("message", message).put("api", "test-v2")
    }

}