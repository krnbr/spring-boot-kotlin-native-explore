package com.neuw.spring_native.web.filters

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.http.server.RequestPath
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.util.pattern.PathPattern
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Mono
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.function.Supplier
import kotlin.collections.ArrayList

@Component
class RequestContextFilter: WebFilter {

    private val logger = LoggerFactory.getLogger(this::class.java)

    lateinit var pathPatternList: ArrayList<PathPattern>

    init {
        // no context changes for the following matched pathPatterns
        val pathPatternActuator: PathPattern = PathPatternParser().parse("/actuator/**")
        pathPatternList = ArrayList()
        pathPatternList.add(pathPatternActuator)
    }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val startTime = System.currentTimeMillis()
        val correlationId: String = if (StringUtils.hasText(exchange.request.headers.getFirst("x-correlation-id"))) {
            exchange.request.headers.getFirst("x-correlation-id")!!
        } else {
            UUID.randomUUID().toString()
        }
        val path: RequestPath = exchange.request.path

        return chain.filter(exchange).contextWrite { context ->
            var context = context
            if (pathPatternList.stream()
                    .anyMatch { pathPattern: PathPattern -> pathPattern.matches(path.pathWithinApplication()) }
            ) {
                context
            } else {
                MDC.put(
                    "correlation-id",
                    correlationId
                ) // MDC does not work well in reactive world, this is kinda redundant
                logger.info(
                    "${exchange.request.method}: url = ${exchange.request.uri.path}, from client IP {}, setting the correlation-id to the request, correlation-id=$correlationId",
                    if (exchange.request.headers.containsKey("X-Forwarded-For")) exchange.request
                        .headers.getFirst("X-Forwarded-For") else exchange.request.remoteAddress?.address
                        ?.hostAddress
                )
                val map: HashMap<Any?, Any?> = HashMap<Any?, Any?>()
                map["correlation-id"] = correlationId
                context = context.put("request-context", map)
                exchange.attributes["correlation-id"] = correlationId
                exchange.response.beforeCommit(Supplier {
                    exchange.response.headers.add("x-correlation-id", correlationId)
                    exchange.response.headers.add(
                        "x-trace-remote",
                        if (exchange.request.headers.containsKey("X-Forwarded-For")) exchange.request
                            .headers.getFirst("X-Forwarded-For") else exchange.request.remoteAddress
                            ?.address?.hostAddress
                    )
                    val totalTime = System.currentTimeMillis() - startTime
                    exchange.response.headers.add("x-trace-time", totalTime.toString() + "ms")
                    exchange.response.headers.add("x-timestamp", ZonedDateTime.now().toEpochSecond().toString())
                    exchange.response.headers
                        .setDate(ZonedDateTime.ofInstant(Date().toInstant(), ZoneId.of("UTC")))
                    Mono.empty<Void?>()
                })
                context
            }
        }.doFinally { signalType ->
            if (!pathPatternList.stream()
                    .anyMatch { pathPattern: PathPattern -> pathPattern.matches(path.pathWithinApplication()) }
            ) {
                val totalTime = System.currentTimeMillis() - startTime
                exchange.attributes["totalTime"] = totalTime
                logger.info(
                    "${exchange.request.method}: url = ${exchange.request.uri.path}, from client IP {}, processed with signalType=$signalType with correlation-id=$correlationId in $totalTime ms",
                    if (exchange.request.headers.containsKey("X-Forwarded-For")) {
                        exchange.request.headers.getFirst("X-Forwarded-For")
                    } else exchange.request.remoteAddress?.address?.hostAddress,
                )
            }
        }
    }
}