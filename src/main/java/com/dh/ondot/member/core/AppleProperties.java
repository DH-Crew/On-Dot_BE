package com.dh.ondot.member.core;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth2.client.registration.apple")
public record AppleProperties(
        String clientId,
        String teamId,
        String keyId,
        String audience,
        String grantType,
        String privateKey
) {
}
