package com.dh.ondot.member.api.response;

import com.dh.ondot.member.domain.Member;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record OnboardingResponse(
        Long memberId,
        LocalDateTime updatedAt
) {
    public static OnboardingResponse from(Member member) {
        return new OnboardingResponse(member.getId(), member.getUpdatedAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime());
    }
}
