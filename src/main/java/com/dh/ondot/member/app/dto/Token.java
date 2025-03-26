package com.dh.ondot.member.app.dto;

public record Token(
        String accessToken,
        String refreshToken
) {
}