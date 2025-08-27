package com.dh.ondot.member.api.response;

import com.dh.ondot.core.util.TimeUtils;
import com.dh.ondot.member.domain.Member;

import java.time.LocalDateTime;

public record OnboardingResponse(
        String accessToken,
        String refreshToken,
        LocalDateTime createdAt
) {
    public static OnboardingResponse from(String accessToken, String refreshToken, Member member) {
        return new OnboardingResponse(accessToken, refreshToken, TimeUtils.toSeoulDateTime(member.getUpdatedAt()));
    }
}
