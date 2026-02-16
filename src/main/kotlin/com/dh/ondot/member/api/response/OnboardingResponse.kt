package com.dh.ondot.member.api.response

import com.dh.ondot.core.util.TimeUtils
import com.dh.ondot.member.domain.Member
import java.time.LocalDateTime

data class OnboardingResponse(
    val accessToken: String,
    val refreshToken: String,
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(accessToken: String, refreshToken: String, member: Member): OnboardingResponse =
            OnboardingResponse(
                accessToken = accessToken,
                refreshToken = refreshToken,
                createdAt = TimeUtils.toSeoulDateTime(member.updatedAt)!!,
            )
    }
}
