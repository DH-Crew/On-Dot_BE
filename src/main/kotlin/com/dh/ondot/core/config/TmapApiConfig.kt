package com.dh.ondot.core.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "tmap")
data class TmapApiConfig(
    val baseUrl: String,
    val appKey: String,
)
