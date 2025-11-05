package com.dh.ondot.member.api.response;

public record LoginResponse(
        Long memberId,
        String accessToken,
        String refreshToken,
        boolean isNewMember
) {
    public static LoginResponse of(Long memberId, String accessToken, String refreshToken, boolean isNewMember) {
        return new LoginResponse(memberId, accessToken, refreshToken, isNewMember);
    }
}
