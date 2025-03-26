package com.dh.ondot.member.app.dto;

import java.util.Date;

public record TokenInfo(
        String tokenId,
        String memberId,
        Date expiration
) {
}
