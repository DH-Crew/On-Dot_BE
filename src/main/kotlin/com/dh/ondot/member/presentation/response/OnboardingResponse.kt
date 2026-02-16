package com.dh.ondot.member.presentation.response

import com.dh.ondot.member.application.dto.OnboardingResult
import java.time.LocalDateTime

data class OnboardingResponse(
    val accessToken: String,
    val refreshToken: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(result: OnboardingResult): OnboardingResponse =
            OnboardingResponse(
                accessToken = result.accessToken,
                refreshToken = result.refreshToken,
                createdAt = result.createdAt,
            )
    }
}
