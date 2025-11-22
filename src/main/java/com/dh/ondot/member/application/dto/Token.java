package com.dh.ondot.member.application.dto;

public record Token(
        String accessToken,
        String refreshToken
) {
}
