package com.dh.ondot.notification.infra.api;

import com.dh.ondot.notification.domain.dto.EmergencyAlertDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmergencyAlertApi {
    private static final DateTimeFormatter YYYYMMDD_SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ObjectMapper objectMapper;

    private RestClient restClient;
    @Value("${external-api.safety-data.base-url}")
    private String baseUrl;
    @Value("${external-api.safety-data.service-key}")
    private String serviceKey;

    @PostConstruct
    public void init() {
        this.restClient = RestClient.create(baseUrl);
    }

    public List<EmergencyAlertDto> fetchAllAlertsByDate(LocalDate date) {
        String formattedDate = date.format(YYYYMMDD);
        String resJson = fetchAlerts(formattedDate);
        JsonNode itemsNode = extractBodyNode(resJson);

        return parseAlerts(itemsNode);
    }

    private String fetchAlerts(String date) {
        return restClient.get()
                .uri(b -> b.queryParam("serviceKey", serviceKey)
                        .queryParam("crtDt", date)
                        .build())
                .retrieve()
                .body(String.class);
    }

    private JsonNode extractBodyNode(String rawJson) {
        try {
            return objectMapper.readTree(rawJson)
                    .path("body");
        } catch (Exception e) {
            log.error("Failed to parse emergency alerts JSON response", e);
            throw new IllegalStateException("Failed to parse emergency alerts JSON response", e);
        }
    }

    private List<EmergencyAlertDto> parseAlerts(JsonNode itemsNode) {
        if (!itemsNode.isArray()) {
            return Collections.emptyList();
        }

        List<EmergencyAlertDto> alerts = new ArrayList<>();
        for (JsonNode node : itemsNode) {
            try {
                alerts.add(mapToDto(node));
            } catch (Exception e) {
                log.error("Failed to map emergency alert JSON node to DTO: {}", node.toString(), e);
            }
        }
        return alerts;
    }

    private EmergencyAlertDto mapToDto(JsonNode node) {
        String content = node.path("MSG_CN").asText("");
        String region = node.path("RCPTN_RGN_NM").asText("");
        String createDateTime = node.path("CRT_DT").asText("");
        LocalDateTime issuedAt = LocalDateTime.parse(createDateTime, YYYYMMDD_SLASH);

        return new EmergencyAlertDto(content, region, issuedAt);
    }
}
