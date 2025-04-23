package com.dh.ondot.schedule.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "odsay")
public record OdsayApiConfig(
        String apiKey
) {
}
