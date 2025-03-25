package com.dh.ondot.member.api.response;

public record UserInfo(
        String oauthProviderId,
        String email
) {
}