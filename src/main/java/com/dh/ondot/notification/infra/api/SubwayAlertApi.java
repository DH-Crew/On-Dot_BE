package com.dh.ondot.notification.infra.api;

import com.dh.ondot.notification.domain.dto.SubwayAlertDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubwayAlertApi {
    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ObjectMapper objectMapper;

    private RestClient restClient;
    @Value("${external-api.seoul-transportation.base-url}")
    private String baseUrl;
    @Value("${external-api.seoul-transportation.service-key}")
    private String serviceKey;

    @PostConstruct
    public void init() {
        this.restClient = RestClient.create(baseUrl);
    }

    public List<SubwayAlertDto> fetchAllAlertsByDate(LocalDate date) {
        String formattedDate = date.format(YYYYMMDD);
        String resJson = fetchAlerts(formattedDate);
        JsonNode itemsNode = extractItemsNode(resJson);

        return parseItems(itemsNode);
    }

    private String fetchAlerts(String date) {
        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("dataType", "JSON")
                .queryParam("srchStartNoftOcrnYmd", date)
                .queryParam("srchEndNoftOcrnYmd", date)
                // build(true) → 컴포넌트들(여기서는 serviceKey 등)이 이미 인코딩된 상태라고 보고 다시 인코딩하지 않도록 처리
                .build(true)
                .toUri();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(String.class);
    }

    private JsonNode extractItemsNode(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);
            return root.path("response")
                    .path("body")
                    .path("items")
                    .path("item");
        } catch (JsonProcessingException e) {
            log.error("Failed to parse subway alert JSON response", e);
            throw new IllegalStateException("Failed to parse subway alert JSON response", e);
        }
    }

    private List<SubwayAlertDto> parseItems(JsonNode itemsNode) {
        if (!itemsNode.isArray()) {
            return Collections.emptyList();
        }

        List<SubwayAlertDto> alerts = new ArrayList<>();
        for (JsonNode node : itemsNode) {
            try {
                alerts.add(mapToDto(node));
            } catch (Exception e) {
                log.error("Failed to map alert JSON node to DTO: {}", node.toString(), e);
            }
        }
        return alerts;
    }

    private SubwayAlertDto mapToDto(JsonNode node) {
        String title = node.path("noftTtl").asText();
        String content = node.path("noftCn").asText();
        String lineName = node.path("lineNmLst").asText();
        String beginDateTime = node.path("xcseSitnBgngDt").asText();
        String notificationOccurrenceTime = node.path("noftOcrnDt").asText();
        LocalDateTime startAt = LocalDateTime.parse(beginDateTime);
        LocalDateTime createdAt = LocalDateTime.parse(notificationOccurrenceTime);

        return new SubwayAlertDto(title, content, lineName, startAt, createdAt);
    }
}
