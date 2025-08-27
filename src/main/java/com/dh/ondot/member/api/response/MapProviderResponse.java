package com.dh.ondot.member.api.response;

import com.dh.ondot.core.util.TimeUtils;
import com.dh.ondot.member.domain.Member;
import com.dh.ondot.member.domain.enums.MapProvider;

import java.time.LocalDateTime;

public record MapProviderResponse(
        MapProvider mapProvider,
        LocalDateTime updatedAt
) {
    public static MapProviderResponse from(Member member) {
        return new MapProviderResponse(
                member.getMapProvider(),
                TimeUtils.toSeoulDateTime(member.getUpdatedAt())
        );
    }
}
