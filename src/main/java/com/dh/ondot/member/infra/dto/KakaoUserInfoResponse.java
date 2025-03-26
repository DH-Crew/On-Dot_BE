package com.dh.ondot.member.infra.dto;

public record KakaoUserInfoResponse(
        Long id,
        KakaoAccount kakao_account
) {
    public record KakaoAccount(
            String email
    ) {
    }
}