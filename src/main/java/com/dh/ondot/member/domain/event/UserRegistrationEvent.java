package com.dh.ondot.member.domain.event;

import com.dh.ondot.member.domain.enums.OauthProvider;

public record UserRegistrationEvent(
        Long memberId,
        String email,
        OauthProvider oauthProvider,
        Long totalMemberCount,
        String mobileType
) {
}
