package com.dh.ondot.member.api.response;

import com.dh.ondot.core.util.TimeUtils;
import com.dh.ondot.member.domain.Member;

import java.time.LocalDateTime;

public record PreparationTimeResponse(
        Integer preparationTime,
        LocalDateTime updatedAt
) {
    public static PreparationTimeResponse from(Member member) {
        return new PreparationTimeResponse(
                member.getPreparationTime(),
                TimeUtils.toSeoulDateTime(member.getUpdatedAt())
        );
    }
}
