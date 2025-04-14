package com.dh.ondot.member.domain;

import com.dh.ondot.member.domain.enums.OauthProvider;
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
    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false)
    private OauthProvider oauthProvider;

    @Column(name = "oauth_provider_id", nullable = false)
    private String oauthProviderId;

    public static OauthInfo of(OauthProvider oauthProvider, String oauthProviderId) {
        return new OauthInfo(oauthProvider, oauthProviderId);
    }
}
