package com.dh.ondot.member.api.response;

public record Token(
        String accessToken,
        String refreshToken
) {
}