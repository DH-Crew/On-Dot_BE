package com.dh.ondot.member.core

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth2.client.registration.apple")
data class AppleProperties(
    val clientId: String,
    val teamId: String,
    val keyId: String,
    val audience: String,
    val grantType: String,
)
