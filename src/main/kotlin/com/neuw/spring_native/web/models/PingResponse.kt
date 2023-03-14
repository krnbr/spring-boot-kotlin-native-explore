package com.neuw.spring_native.web.models

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class PingResponse(val message: String, val version: String)