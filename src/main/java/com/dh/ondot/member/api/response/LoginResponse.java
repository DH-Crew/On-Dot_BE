package com.dh.ondot.member.api.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        boolean isOnboardingCompleted
) {
    public static LoginResponse of(String accessToken, String refreshToken, boolean isOnboardingCompleted) {
        return new LoginResponse(accessToken, refreshToken, isOnboardingCompleted);
    }
}
