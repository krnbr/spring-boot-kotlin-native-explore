package com.neuw.spring_native.web.models

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonPropertyOrder
import org.springframework.http.HttpStatus
import java.text.SimpleDateFormat
import java.util.*

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder(value = ["status", "message", "method", "path", "data", "time", "requestId", "correlationId"])
data class ServiceResponse(var status: Int, @JsonProperty("request_id") val requestId: String = UUID.randomUUID().toString(), val time: String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Date())) {
    val message: String ? = null
    @JsonProperty("correlation_id") var correlationId: String? = null
    var path: String? = null
    var method: String? = null
    var data: Any? = null
    constructor() : this(HttpStatus.OK.value())
}