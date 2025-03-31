package com.dh.ondot.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OauthInfo {
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OauthProvider oauthProvider;

    @Column(nullable = false)
    private String oauthProviderId;

    public static OauthInfo of(OauthProvider oauthProvider, String oauthProviderId) {
        return new OauthInfo(oauthProvider, oauthProviderId);
    }
}
