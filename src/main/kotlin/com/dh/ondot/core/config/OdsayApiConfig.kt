package com.dh.ondot.core.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "odsay")
data class OdsayApiConfig(
    val baseUrl: String,
    val apiKey: String,
)
