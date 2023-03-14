package com.neuw.spring_native.config.handlers

import com.fasterxml.jackson.databind.node.ObjectNode
import com.neuw.spring_native.annotations.ApiResponse
import com.neuw.spring_native.web.models.PingResponse
import com.neuw.spring_native.web.models.ServiceResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.core.KotlinDetector
import org.springframework.core.MethodParameter
import org.springframework.http.codec.HttpMessageWriter
import org.springframework.util.Assert
import org.springframework.web.reactive.HandlerResult
import org.springframework.web.reactive.accept.RequestedContentTypeResolver
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class CustomResponseBodyResultHandler(writers: List<HttpMessageWriter<*>?>?, resolver: RequestedContentTypeResolver?) :
    ResponseBodyResultHandler(writers!!, resolver!!) {

    val log: Logger = LoggerFactory.getLogger(this::class.java)

    @RegisterReflectionForBinding(value = [ApiResponse::class, ObjectNode::class, ServiceResponse::class, PingResponse::class])
    override fun supports(result: HandlerResult): Boolean {
        val className = result.returnTypeSource.declaringClass.name;
        val methodName = result.returnTypeSource.method?.name;
        val classReference: Array<Annotation> = result.returnTypeSource.declaringClass.annotations
        val methodAnnotations: Array<Annotation> = result.returnTypeSource.methodAnnotations
        if (classReference.any { it is ApiResponse }) {
            // if class is marked with ApiResponse annotation
            if (logger.isDebugEnabled) {
                logger.debug("$className is marked with ApiResponse annotation")
            } else {
                logger.info("$className is marked with ApiResponse annotation")
            }
            return true;
        } else if (methodAnnotations.any { it is ApiResponse }) {
            // if method is marked with ApiResponse annotation
            if (logger.isDebugEnabled) {
                logger.debug("$methodName inside $className is marked with ApiResponse annotation")
            } else {
                logger.info("$methodName inside $className is marked with ApiResponse annotation")
            }
            return true;
        } else {
            val classInterfaces = result.returnTypeSource.declaringClass.interfaces
            // check for the interfaces if class is implementations against an interface
            classInterfaces.forEach {
                if (it.isAnnotationPresent(ApiResponse::class.java)) {
                    if (logger.isDebugEnabled) {
                        logger.debug("$className's interface is marked with ApiResponse annotation")
                    } else {
                        logger.info("$className's interface is marked with ApiResponse annotation")
                    }
                    return true
                }
            }
        }

        return false
    }

    @RegisterReflectionForBinding(value = [Mono::class, Object::class, ObjectNode::class, ServiceResponse::class, PingResponse::class])
    override fun handleResult(exchange: ServerWebExchange, result: HandlerResult): Mono<Void> {
        val returnValueMono: Mono<*>
        val bodyParameter: MethodParameter
        val adapter = getAdapter(result)
        val actualParameter = result.returnTypeSource

        if (adapter != null) { // if the response was mapped in Mono
            Assert.isTrue(!adapter.isMultiValue, "Only a single value supported")
            returnValueMono = Mono.from(adapter.toPublisher<Any>(result.returnValue))
            val isContinuation = KotlinDetector.isSuspendingFunction(actualParameter.method!!) &&
                    COROUTINES_FLOW_CLASS_NAME != actualParameter.parameterType.name
            bodyParameter = if (isContinuation) {
                logger.info("actualParameter nested isContinuation $isContinuation")
                actualParameter.nested()
            } else {
                logger.info("actualParameter isContinuation $isContinuation")
                actualParameter
            }
        } else { // if the response was not mapped in Mono
            logger.info("not mono")
            returnValueMono = Mono.justOrEmpty(result.returnValue)
            bodyParameter = actualParameter
        }

        // construct an overlapping response that maps to ServiceResponse
        val bodyNew: Mono<ServiceResponse> = returnValueMono.map { o: Any ->
            log.info("transforming the stuff {}", o)
            val res = ServiceResponse(exchange.response.statusCode!!.value())
            res.status = exchange.response.statusCode!!.value() // if someone has override the response status, we will use that
            res.data = o
            res.method = exchange.request.method.toString()
            res.path = exchange.request.path.toString()
            res.correlationId = exchange.attributes["correlation-id"].toString()
            return@map res
        }

        return writeBody(bodyNew, bodyParameter, exchange)
    }

}
