package com.dabwish.dabwish.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class Message(
    val message: String,
    val buildNumber: String
)

@RestController
@RequestMapping("/hello")
class HelloController(
    @Value("\${dabwish.buildNumber}") val buildNumber: String
) {

    val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping
    fun hello(): Message {
        logger.trace("TRACE line")
        logger.debug("DEBUG line")
        logger.info("INFO line")
        logger.warn("WARNING line")
        logger.warn("ERROR line")

        return Message("HelloWorld", buildNumber)
    }
}