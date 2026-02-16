package com.dh.ondot.member.core

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val accessTokenExpireTimeInHours: Long,
    val refreshTokenExpireTimeInHours: Long,
)
