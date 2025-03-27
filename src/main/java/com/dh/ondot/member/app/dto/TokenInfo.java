package com.dh.ondot.member.app.dto;

import java.time.Instant;

public record TokenInfo(
        String tokenId,
        String memberId,
        Instant expiration
) {
}
