package com.dh.ondot.notification.infra.emergency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmergencyAlertJsonExtractor {
    private final ObjectMapper objectMapper;

    public JsonNode extractAlerts(String rawJson) {
        try {
            return objectMapper.readTree(rawJson)
                    .path("body");
        } catch (JsonProcessingException e) {
            log.error("[EmergencyAlertJsonExtractor] JSON parsing failed", e);
            throw new IllegalStateException("[EmergencyAlertJsonExtractor] JSON parsing failed", e);
        }
    }
}
