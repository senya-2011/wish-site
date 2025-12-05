package com.dabwish.dabwish.config

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Aspect
@Component
@ConditionalOnProperty(prefix = "app.cache.logging", name = ["enabled"], havingValue = "true")
class MethodCallLoggingAspect {
    private val logger = LoggerFactory.getLogger(MethodCallLoggingAspect::class.java)

    @Around("execution(* com.dabwish.dabwish.service..*(..))")
    fun logInvocation(joinPoint: ProceedingJoinPoint): Any? {
        logger.info("[AOP] invoke {}", joinPoint.signature.toShortString())
        return joinPoint.proceed()
    }
}

