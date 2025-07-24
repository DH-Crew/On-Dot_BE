package com.dh.ondot.notification.infra.subway;

import com.dh.ondot.notification.domain.dto.SubwayAlertDto;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.LocalDateTime;

@Slf4j
@Component
public class SubwayAlertDtoMapper {

    public SubwayAlertDto toDto(JsonNode node) {
        String title = node.path("noftTtl").asText("");
        String content = node.path("noftCn").asText("");
        String lineName = node.path("lineNmLst").asText("");

        String notificationOccurTime = node.path("noftOcrnDt").asText("");
        String eventBeginAt = node.path("xcseSitnBgngDt").asText("");

        LocalDateTime createdAt;
        LocalDateTime startAt;
        try {
            createdAt = LocalDateTime.parse(notificationOccurTime);
            startAt = (eventBeginAt.isBlank())
                    ? createdAt
                    : LocalDateTime.parse(eventBeginAt);
        } catch (DateTimeException ex) {
            log.error("Failed to parse datetime fields: begin={}, notif={}", eventBeginAt, notificationOccurTime, ex);
            throw new IllegalArgumentException("Invalid datetime format in subway alert data", ex);
        }

        return new SubwayAlertDto(title, content, lineName, startAt, createdAt);
    }
}
