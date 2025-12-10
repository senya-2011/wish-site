package com.dabwish.dabwish.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component


private val logger = KotlinLogging.logger {}

@Aspect
@Component
@ConditionalOnProperty(prefix = "app.cache.logging", name = ["enabled"], havingValue = "true")
class MethodCallLoggingAspect {
    @Around("execution(* com.dabwish.dabwish.service..*(..))")
    fun logInvocation(joinPoint: ProceedingJoinPoint): Any? {
        logger.info { "${"[AOP] invoke {}"} ${joinPoint.signature.toShortString()}" }
        return joinPoint.proceed()
    }
}

