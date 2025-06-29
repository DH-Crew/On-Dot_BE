package com.dh.ondot.member.api.response;

import com.dh.ondot.member.domain.Member;

import java.time.LocalDateTime;
import java.time.ZoneId;

public record UpdateMapProviderResponse(
        Long memberId,
        LocalDateTime updatedAt
) {
    public static UpdateMapProviderResponse from(Member member) {
        return new UpdateMapProviderResponse(member.getId(), member.getUpdatedAt().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime());
    }
}
