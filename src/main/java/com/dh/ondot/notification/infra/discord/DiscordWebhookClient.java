package com.dh.ondot.notification.infra.discord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordWebhookClient {
    private final RestClient restClient;

    @Value("${external-api.discord.webhook.url}")
    private String discordUrl;

    public void sendMessage(String message) {
        Map<String, Object> payload = createPayload(message);

        restClient.post()
                .uri(discordUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }

    private Map<String, Object> createPayload(String message) {
        return Map.of("content", message);
    }
}
