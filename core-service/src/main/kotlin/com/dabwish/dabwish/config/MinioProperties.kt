package com.dabwish.dabwish.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "minio")
data class MinioProperties(
    val url: String,
    val accessKey: String,
    val secretKey: String,
    val bucketName: String,
)
