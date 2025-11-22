package com.dh.ondot.notification.infra.subway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubwayAlertJsonExtractor {
    private final ObjectMapper objectMapper;

    public JsonNode extractAlerts(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            return root.path("response")
                    .path("body")
                    .path("items")
                    .path("item");
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Subway alert JSON 파싱 실패", ex);
        }
    }
}
