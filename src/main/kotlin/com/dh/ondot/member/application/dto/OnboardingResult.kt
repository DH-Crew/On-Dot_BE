package com.dh.ondot.member.application.dto

import java.time.LocalDateTime

data class OnboardingResult(
    val accessToken: String,
    val refreshToken: String,
    val createdAt: LocalDateTime,
)
