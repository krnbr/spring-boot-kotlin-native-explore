package com.neuw.spring_native.annotations

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.CLASS
)
@Retention(
    AnnotationRetention.RUNTIME
)
annotation class ApiResponse